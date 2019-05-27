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
import java.util.List;
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

import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.reflect.SavageReflection;
import jp.furplag.sandbox.stream.Streamr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class EntityInspectorTest {


  public static class NotAnEntity implements EntityOrigin {}

  public static interface Zero extends EntityOrigin {

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
        public static class Three extends Zero.One.Two {
          @Entity
          public static class Four extends Zero.One.Two.Three {}
        }
      }
    }
  }

  @Entity
  public static class One implements EntityOrigin {}

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
    public int notPersistive;

    public int five;

    public int six;

    @Column(name = "se7en")
    public int seven;

    public Five(int five, int six, int seven) {
      this(0, five, six, seven);
    }
  }

  @Test
  @SuppressWarnings({ "unchecked" })
  void test() {
    // @formatter:off
    assertAll(
        () -> assertArrayEquals(new Class<?>[] { Zero.One.class }, ((List<Class<?>>) SavageReflection.get(new Zero.One().inspector(), "classes")).toArray(Class<?>[]::new))
      , () -> assertArrayEquals(new Class<?>[] { Zero.One.Two.Three.Four.class, Zero.One.Two.Three.class, Zero.One.Two.class, Zero.One.class }, ((List<Class<?>>) SavageReflection.get(new Zero.One.Two.Three.Four().inspector(), "classes")).toArray(Class<?>[]::new))
    );
    // @formatter:on
  }

  @Test
  void nameConverting() {
    // @formatter:off
    assertAll(
        () -> assertEquals("one", new Zero.One().getName())
      , () -> assertEquals("two", new Zero.One.Two().getName())
      , () -> assertEquals("three", new Zero.One.Two.Three().getName())
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertEquals("one, two, three, four, five, six, se7en", new Zero.One().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("one, two, three, four, five, six, se7en", new Zero.One.Two().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("ONE, TWO, three, FOUR, FIVE, SIX, se7en", new Zero.One.Two.Three().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
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
