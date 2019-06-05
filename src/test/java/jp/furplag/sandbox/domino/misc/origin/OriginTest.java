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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

class OriginTest {

  public static final org.seasar.doma.jdbc.Config config = TestConfig.singleton();

  @Data
  public static class Zero implements Origin {

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
        () -> assertEquals("select * from OriginTest$1", new Origin() {}.select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from default", new Origin() { @Override public String defaultName() { return "default";}}.select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(config)).getSql().toString())

      , () -> assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from TWO", new Zero.One.Two().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from four", new Zero.One.Two.Three.Four().select(SelectBuilder.newInstance(config)).getSql().toString())
      , () -> assertEquals("select * from five_SIX_7", new Zero.One.Two.Three.Four.Five().select(SelectBuilder.newInstance(config)).getSql().toString())
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
        () -> assertEquals(NamingType.NONE, new Origin() {}.getNamingType())
      , () -> assertEquals(NamingType.NONE, new Zero().getNamingType())
      , () -> assertEquals(NamingType.UPPER_CASE, new Zero.One().getNamingType())
      , () -> assertEquals(NamingType.UPPER_CASE, new Zero.One.Two().getNamingType())
      , () -> assertEquals(NamingType.SNAKE_LOWER_CASE, new Zero.One.Two.Three().getNamingType())
      , () -> assertEquals(NamingType.SNAKE_LOWER_CASE, new Zero.One.Two.Three.Four().getNamingType())
      , () -> assertEquals(NamingType.SNAKE_LOWER_CASE, new Zero.One.Two.Three.Four.Five().getNamingType())
    );
    // @formatter:on
  }

}
