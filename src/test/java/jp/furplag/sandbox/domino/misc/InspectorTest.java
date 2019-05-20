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
package jp.furplag.sandbox.domino.misc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import lombok.Data;
import lombok.EqualsAndHashCode;

class InspectorTest {

  public static class Zero implements RowOrigin {
    @Entity(naming = NamingType.SNAKE_LOWER_CASE)
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class One extends Zero {
      @Id
      Long zero;

      @Column
      String one;

      @Column(name = "two")
      String TwO;

      String threeFourFive;

      @Transient
      int six;

      @Column
      @Transient
      int seven;

      public static class Two extends Zero.One {
        @Entity(naming = NamingType.UPPER_CASE)
        public static class Three extends Zero.One.Two {
          @Table(name = "fourFiveSix")
          @Entity(naming = NamingType.SNAKE_LOWER_CASE)
          public static class Four extends Zero.One.Two.Three {
            @Table(name = "")
            public static class FiveSix7Eight extends Zero.One.Two.Three.Four {}
          }
        }
      }
    }
  }

  @Test
  void test() {
    assertAll(
          () -> assertNull(Inspector.getName(null))
        , () -> assertEquals("one", Inspector.getName(new Zero.One().getClass()))
        , () -> assertEquals("two", Inspector.getName(new Zero.One.Two().getClass()))
        , () -> assertEquals("THREE", Inspector.getName(new Zero.One.Two.Three().getClass()))
        , () -> assertEquals("fourFiveSix", Inspector.getName(new Zero.One.Two.Three.Four().getClass()))
        , () -> assertEquals("five_six7_eight", Inspector.getName(new Zero.One.Two.Three.Four.FiveSix7Eight().getClass()))
    );
  }

}
