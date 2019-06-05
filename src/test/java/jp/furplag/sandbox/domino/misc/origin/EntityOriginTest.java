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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

class EntityOriginTest {

  public static final org.seasar.doma.jdbc.Config config = TestConfig.singleton();

  @Data
  public static class Zero implements EntityOrigin {

    @Column(name = "whatever")
    private static final String nope = "this is not a database column .";
    @Entity(naming = NamingType.UPPER_CASE)
    public static class One extends Zero {

      @Transient
      int zero;

      @Id
      long one;

      String two;

      @Column(name = "thr33")
      String three;

      Four four;

      Five five;

      @Transient
      Five six;

      public static class Two extends Zero.One {
        @Entity(naming = NamingType.SNAKE_LOWER_CASE)
        public static class Three extends Zero.One.Two {

          long thr33;

          @Entity(naming = NamingType.NONE)
          @Table(name = "")
          public static class Four extends Zero.One.Two.Three {
            @Table(name = "five_SIX_7")
            public static class Five extends Zero.One.Two.Three.Four {

            }
          }
        }
      }
    }
  }

  @Value
  @Domain(valueType = int.class)
  public static class Four {
    int value;
  }

  @Value
  @RequiredArgsConstructor
  @Embeddable
  public static class Five {

    @Transient
    int zeroOneTwoThreeFour;

    int five;

    int six;

    @Column(name = "se7en")
    int seven;

    public Five(int five, int six, int seven) {
      this(0, five, six, seven);
    }
  }

  @Test
  void test() {
    // @formatter:off
    assertAll(
        () -> assertEquals("select * from EntityOriginTest$1", new EntityOrigin() {}.select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from default", new EntityOrigin() { @Override public String defaultName() { return "default";}}.select(SelectBuilder.newInstance(config)).getSql().toString())

      , () -> assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from TWO", new Zero.One.Two().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select one, thr33, two, four, five, six, se7en from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select one, thr33, two, four, five, six, se7en from four", new Zero.One.Two.Three.Four().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select one, thr33, two, four, five, six, se7en from five_SIX_7", new Zero.One.Two.Three.Four.Five().select(SelectBuilder.newInstance(config)).getSql().toString())

      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), (String) null).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), (String[]) null).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), new String[] {}).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), null, null, null).getSql().toString())
      , () -> assertEquals("select ONE, TWO, thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "ONE", "TWO", "thr33").getSql().toString())
      , () -> assertEquals("select thr33, FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "one", "two", "thr33").getSql().toString())
      , () -> assertEquals("select FOUR, FIVE, SIX, se7en from ONE", new Zero.One().select(SelectBuilder.newInstance(config), "one", "two", "three").getSql().toString())
      , () -> assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config), Inspector.of(Zero.One.class).getFields().stream().map(Field::getName).toArray(String[]::new)).getSql().toString())

      , () -> assertEquals("select one, thr33, two, four, five, six, se7en from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(config), "three").getSql().toString())
      , () -> assertEquals("select one, two, four, five, six, se7en from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(config), "thr33").getSql().toString())
      , () -> assertEquals(long.class, new Zero.One.Two.Three().inspector().getFields((t) -> new Zero.One.Two.Three().inspector().getName(t), "three").stream().filter((t) -> "thr33".equals(new Zero.One.Two.Three().inspector().getName(t))).map(Field::getType).findFirst().orElse(null))
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
  }
}
