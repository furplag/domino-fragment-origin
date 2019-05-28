/**
 * Copyright (C) 2019+ furplag (https://github.com/furplag)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.furplag.sandbox.domino.misc.origin;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.vars.Where;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

class ConditionalTest {

  @Entity
  public static class Zero implements Conditional {
    @Transient
    @Getter
    private final Map<String, Where<?>> wheres = new LinkedHashMap<>();

    @EqualsAndHashCode(callSuper = true)
    @Entity(naming = NamingType.UPPER_CASE)
    public static class One extends Zero {

      public static final int nope = 123;

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      public long primaryKey;

      @Column(name = "rename_this_field")
      public int alternate;

      public Toggle toggle;

      public Abc abc;

      @Transient
      public int ignore;

      @EqualsAndHashCode(callSuper = true)
      @Entity
      public static class Two extends One {

        @EqualsAndHashCode(callSuper = true)
        @Entity(naming = NamingType.SNAKE_LOWER_CASE)
        public static class Three extends Two {

          @EqualsAndHashCode(callSuper = true)
          @Entity
          @Table(name = "")
          public static class Four extends Three {

            public long alternate;

            @EqualsAndHashCode(callSuper = true)
            @Entity
            @Table(name = "five_six.se7en")
            public static class Five extends Four {
              public Abc abc;
            }
          }
        }
      }
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Domain(valueType = String.class)
  public static class Toggle {
    public String value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Embeddable
  public static class Abc {
    @Transient
    public int ignore;

    public String a;

    public String b;

    @Column(name = "C")
    public String c;

    public Abc(String a, String b, String c) {
      this(0, a, b, c);
    }
  }

  @Test
  void test() {
    assertAll(
        () -> assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
    );
    final Zero.One one = new Zero.One();
    one.where(one.getColumns().stream());
    assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE  where PRIMARYKEY = ? and rename_this_field = ? and TOGGLE is null and A is null and B is null and C is null", one.select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString().replaceAll("\\r?\\n", ""));
    assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE  where PRIMARYKEY = ? and rename_this_field = ?", one.select(SelectBuilder.newInstance(TestConfig.singleton()), one.getColumns().stream().filter((t) -> Objects.isNull(t.getValue())).map(Map.Entry::getKey).toArray(String[]::new)).getSql().toString().replaceAll("\\r?\\n", ""));
    assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE  where PRIMARYKEY = ?", one.select(SelectBuilder.newInstance(TestConfig.singleton()), one.getColumns().stream().filter((t) -> !Inspector.isIdentity.test(t.getField())).map(Map.Entry::getKey).toArray(String[]::new)).getSql().toString().replaceAll("\\r?\\n", ""));
  }

}
