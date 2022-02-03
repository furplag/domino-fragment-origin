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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import jp.furplag.sandbox.domino.misc.vars.Where;
import jp.furplag.sandbox.reflect.SavageReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

class ConditionallyTest {

  public static final org.seasar.doma.jdbc.Config config = TestConfig.singleton();

  @Entity
  public static class Zero implements Conditionally {

    @Transient
    @Getter
    Map<String, Where<?>> wheres = new ConcurrentHashMap<>();

    @Transient
    @Getter
    Queue<Sequentially.OrderBy> order = new ConcurrentLinkedQueue<>();

    @EqualsAndHashCode(callSuper = true)
    @Entity(naming = NamingType.UPPER_CASE)
    public static class One extends Zero implements Conditionally {

      public static final int nope = 123;

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      public long primaryKey; // = 1;

      @Column(name = "rename_this_field")
      public int alternate; // = 2;

      public Toggle toggle = new Toggle("on");

      public Abc abc = new Abc(3, "AAA", "BBB", "CCC");

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
            @Table(name = "five_SIX_se7en")
            public static class Five extends Four {
              public Abc abc = new Abc(5, "DDD", "EEE", "FFF");
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
  void paintItGreen() {
    assertEquals(true, new Zero.One().where((String) null, null).getWheres().isEmpty());
    assertEquals(true, new Zero.One().where((Where<?>) null).getWheres().isEmpty());
    assertEquals(true, new Zero.One().where("none", null).getWheres().isEmpty());
    assertEquals(true, new Zero.One().where("ignore", null).getWheres().isEmpty());
    assertEquals(true, new Zero.One().where("primaryKey", null).getWheres().isEmpty());
    assertEquals(false, new Zero.One().where("primaryKey", Where.Operator.Equal).getWheres().isEmpty());
  }

  @Test
  void test() {
    /* @formatter:off */
    assertEquals("select * from", new RowOrigin() {}.select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from", new RowOrigin() { @Override public String defaultName() { return "default";}}.select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from TWO", new Zero.One.Two().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from four", new Zero.One.Two.Three.Four().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from five_SIX_se7en", new Zero.One.Two.Three.Four.Five().select(SelectBuilder.newInstance(config)).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), (String) null).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), (String[]) null).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {}).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), (String[]) null, null, null).getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "ONE", "TWO", "thr33").getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "one", "two", "thr33").getSql().toString());
    assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "one", "two", "three").getSql().toString());

    assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {"none"}, new String[] {}).getSql().toString());
    assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {"PRIMARYKEY", "rename_this_field"}, new String[] {}).getSql().toString());
    assertEquals("select PRIMARYKEY, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {"alternate"}, new String[] {}).getSql().toString());
    assertEquals("select PRIMARYKEY, rename_this_field, A, B from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {"one", "toggle", "c"}, new String[] {}).getSql().toString());
    /* @formatter:on */

    assertEquals("select * from ONE  where  PRIMARYKEY = ? and  rename_this_field = ? and  TOGGLE = ? order by PRIMARYKEY", new Zero.One().autoSelect(SelectBuilder.newInstance(config), true).getSql().toString());
    assertEquals("select * from ONE  where  PRIMARYKEY = ? and  A is NULL and  rename_this_field = ? and  B is NULL and  C is NULL and  TOGGLE = ? order by PRIMARYKEY", new Zero.One().autoSelect(SelectBuilder.newInstance(config), false).getSql().toString());

    final Zero.One one = new Zero.One();
    one.toggle = null;
    one.where("a", Where.Operator.Equal, "s");
    one.where("c", Where.Operator.NotNull);
    one.where("primaryKey", Where.Operator.Includes, 1L, 2L, 4L);
    assertEquals("select * from ONE  where  PRIMARYKEY in (?, ?, ?) and  A = ? and  rename_this_field = ? and  not C is NULL order by PRIMARYKEY", one.autoSelect(SelectBuilder.newInstance(config), true).getSql().toString());
    assertEquals("select * from ONE  where  PRIMARYKEY in (?, ?, ?) and  A = ? and  rename_this_field = ? and  B is NULL and  not C is NULL and  TOGGLE is NULL order by PRIMARYKEY", one.autoSelect(SelectBuilder.newInstance(config), false).getSql().toString());
  }
}
