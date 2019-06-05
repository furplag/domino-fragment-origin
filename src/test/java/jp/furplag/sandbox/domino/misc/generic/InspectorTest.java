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

package jp.furplag.sandbox.domino.misc.generic;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.stream.Streamr;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

class InspectorTest {

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

      @Entity
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

  @Data
  public static class One implements EntityOrigin {
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
        () -> assertArrayEquals(new Class<?>[] { Zero.One.class }, new Zero.One().inspector().getClasses().toArray(Class<?>[]::new))
      , () -> assertArrayEquals(new Class<?>[] { Zero.One.Two.Three.Four.class, Zero.One.Two.Three.class, Zero.One.Two.class, Zero.One.class }, new Zero.One.Two.Three.Four().inspector().getClasses().toArray(Class<?>[]::new))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Inspector.of(null))
      , () -> assertEquals(new Origin() {}.inspector(), Inspector.of(Origin.class))
      , () -> assertEquals(Inspector.of(Origin.class), Inspector.of(Zero.class))
      , () -> assertEquals(Inspector.of(Zero.One.class), Inspector.of(Zero.One.class))
      , () -> assertEquals(new Zero.One().inspector(), Inspector.of(Zero.One.class))
      , () -> assertNotEquals(Inspector.of(Zero.One.class), Inspector.of(Zero.One.Two.class))
      , () -> assertNotEquals(Inspector.of(Zero.class), Inspector.of(Zero.One.class))
      , () -> assertNotEquals(Inspector.of(One.class), Inspector.of(Zero.One.class))
    );
    // @formatter:on
  }

  @Test
  void testNamingType() {
    // @formatter:off
    assertAll(
        () -> assertEquals("ONE", new Zero.One().inspector().getName())
      , () -> assertEquals("TWO", new Zero.One.Two().inspector().getName())
      , () -> assertEquals("three", new Zero.One.Two.Three().inspector().getName())
      , () -> assertEquals("four", new Zero.One.Two.Three.Four().inspector().getName())
      , () -> assertEquals("One", new One().inspector().getName())
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertEquals("", new Zero().inspector().getFields().stream().map((t) -> new Zero().inspector().getName(t)).collect(Collectors.joining(", ")))
      , () -> assertEquals("", new One().inspector().getFields().stream().map((t) -> new One().inspector().getName(t)).collect(Collectors.joining(", ")))
      , () -> assertEquals("ONE, TWO, thr33, FOUR, FIVE, SIX, se7en", new Zero.One().inspector().getFields().stream().map((t) -> new Zero.One().inspector().getName(t)).collect(Collectors.joining(", ")))
      , () -> assertEquals("ONE, TWO, thr33, FOUR, FIVE, SIX, se7en", new Zero.One.Two().inspector().getFields().stream().map((t) -> new Zero.One.Two().inspector().getName(t)).collect(Collectors.joining(", ")))
      , () -> assertEquals("one, thr33, two, four, five, six, se7en", new Zero.One.Two.Three().inspector().getFields().stream().map((t) -> new Zero.One.Two.Three().inspector().getName(t)).collect(Collectors.joining(", ")))
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
        () -> assertEquals("one", Streamr.Filter.filtering(Inspector.of(Zero.One.class).getFields(), Inspector.Predicates::isIdentity).map(Field::getName).collect(Collectors.joining(", ")))
      , () -> assertEquals("four", Streamr.Filter.filtering(Inspector.of(Zero.One.class).getFields(), Inspector.Predicates::isDomain).map(Field::getName).collect(Collectors.joining(", ")))
      , () -> assertEquals("one, two, three, four, five, six, seven", Streamr.Filter.filtering(Inspector.of(Zero.One.class).getFields(), Inspector.Predicates::isPersistive).map(Field::getName).collect(Collectors.joining(", ")))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertEquals("long, String, String, Four, int, int, int", Inspector.of(Zero.One.class).getFields().stream().map(Field::getType).map(Class::getSimpleName).collect(Collectors.joining(", ")))
      , () -> assertEquals("long, String, String, Four, int, int, int", Inspector.of(Zero.One.Two.class).getFields().stream().map(Field::getType).map(Class::getSimpleName).collect(Collectors.joining(", ")))
      , () -> assertEquals("long, long, String, Four, int, int, int", Inspector.of(Zero.One.Two.Three.class).getFields().stream().map(Field::getType).map(Class::getSimpleName).collect(Collectors.joining(", ")))
    );
    // @formatter:on
  }

}
