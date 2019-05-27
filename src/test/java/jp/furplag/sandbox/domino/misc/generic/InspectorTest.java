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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class InspectorTest {

  public static class NotAnEntity implements Origin {}

  public static interface Zero extends Origin {

    @Entity(naming = NamingType.LOWER_CASE)
    public static class One implements Zero {

      public static final String nope = "Nope";

      @Transient
      public int zero;

      @Id
      public String one;

      public String two;

      @Column(name = "three")
      public String alternate;

      public Four four;

      public Five five;

      @Table(name = "")
      @Entity
      public static class Two extends Zero.One {
        @Table(name = "three")
        @Entity(naming = NamingType.UPPER_CASE)
        public static class Alternate extends Zero.One.Two {
          @Entity
          public static class Four extends Zero.One.Two.Alternate {}
        }
      }
    }
  }

  @Entity
  public static class One implements Origin {}

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Domain(acceptNull = false, valueType = boolean.class)
  public static final class Four {
    public static final String nope = "Nope";

    public boolean value;

    public boolean getValue() {
      return value;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Embeddable
  public static final class Five {
    public static final String nope = "Nope";

    @Transient
    public int zero;

    public int one;

    public int two;

    @Column(name = "four")
    public int three;

    public Five(int one, int two, int three) {
      this(0, one, two, three);
    }
  }

  @Test
  void test() {
    // @formatter:off
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Inspector.defaultInspector(null))
      , () -> assertThrows(IllegalArgumentException.class, () -> Inspector.defaultInspector(Origin.class))
      , () -> assertThrows(IllegalArgumentException.class, () -> new NotAnEntity().inspector())
      , () -> assertNotNull(Inspector.defaultInspector(One.class))
    );
    // @formatter:on
  }

  @Test
  void testNamingType() {
    // @formatter:off
    assertAll(
        () -> assertEquals("one", new Zero.One().getName())
      , () -> assertEquals("two", new Zero.One.Two().getName())
      , () -> assertEquals("three", new Zero.One.Two.Alternate().getName())
      , () -> assertEquals("FOUR", new Zero.One.Two.Alternate.Four().getName())
      , () -> assertEquals("One", new One().getName())
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
          () -> assertEquals("one", Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isIdentity).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("four", Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isDomain).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("five", Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isEmbeddable).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("one, two, alternate, four, five", Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isPersistive).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals(Stream.concat(Stream.of("nope", "zero"), Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Field::isSynthetic).map(Field::getName)).collect(Collectors.joining(", ")), Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isNotPersistive).map(Field::getName).collect(Collectors.joining(", ")))
    );
    // @formatter:on
  }
}
