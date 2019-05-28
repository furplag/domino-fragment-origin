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
package jp.furplag.sandbox.domino.misc.vars;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.reflect.Reflections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

class VarTest {

  @Entity
  public static class Zero implements EntityOrigin {

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
    // @formatter:off
    assertAll(
        () -> assertEquals(0L, new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).getValue())
      , () -> assertEquals(long.class, new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).getValueType())
      , () -> assertNull(new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "abc")).getValue())
      , () -> assertEquals(Toggle.class, new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getValueType())
      , () -> assertEquals(new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getFieldName(), new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getKey())
    );
    // @formatter:on
  }

  @Test
  void testEntity() {
    // @formatter:off
    assertAll(
        () -> assertEquals("PRIMARYKEY", new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).flatternyze().map(Var::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("A, B, C", new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "abc")).flatternyze().map(Var::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("PRIMARYKEY", new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).flatternyze().map(Var::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("PRIMARYKEY, rename_this_field, TOGGLE, A, B, C", new Zero.One.Two().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two(), t)).flatMap(Var.Origin::flatternyze).map(Var::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", new Zero.One.Two.Three().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze).map(Var::getColumnName).collect(Collectors.joining(", ")))
    );
    // @formatter:on
    final SelectBuilder selectBuilder = SelectBuilder.newInstance(TestConfig.singleton());
    final AtomicReference<String> comma = new AtomicReference<>(" ");
    new Zero.One.Two.Three().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze).forEach((t) -> t.sql(selectBuilder, comma.getAndSet(", ")));
    assertEquals("primary_key, rename_this_field, toggle, a, b, C", selectBuilder.getSql().toString().replaceAll("\\r\\n", ""));
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(null, null))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(null, Reflections.getField(Zero.One.class, "primaryKey")))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "notExists")))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(null, null))
      , () -> assertThrows(UnsupportedOperationException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).setValue(123L))
      , () -> assertEquals("PRIMARYKEY", new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(SelectBuilder.newInstance(TestConfig.singleton()), null).getSql().toString().replaceAll("\\r\\n", ""))
      , () -> assertEquals("PRIMARYKEY", new Zero.One().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted().map(Var::getColumnName).findFirst().orElse(null))
      , () -> assertEquals("rename_this_field", new Zero.One().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted(Comparator.reverseOrder()).map(Var::getColumnName).findFirst().orElse(null))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(null, null))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(null, Reflections.getField(Zero.One.class, "primaryKey")))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "notExists")))
      , () -> assertThrows(NullPointerException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(null, null))
      , () -> assertThrows(UnsupportedOperationException.class, () -> new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).setValue(123L))
      , () -> assertEquals("PRIMARYKEY", new Var.Origin<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(SelectBuilder.newInstance(TestConfig.singleton()), null).getSql().toString().replaceAll("\\r\\n", ""))
      , () -> assertEquals("PRIMARYKEY", new Zero.One().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted().map(Var::getColumnName).findFirst().orElse(null))
      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", Var.map(new Zero.One.Two.Three().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Var::getColumnName).keySet().stream().collect(Collectors.joining(", ")))
      , () -> assertEquals("primaryKey, alternate, toggle, a, b, c", Var.map(new Zero.One.Two.Three().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
      , () -> assertEquals("primary_key, alternate, toggle, a, b, C", Var.map(new Zero.One.Two.Three.Four().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Var::getColumnName).keySet().stream().collect(Collectors.joining(", ")))
      , () -> assertEquals("primaryKey, a, b, c, alternate, toggle", Var.map(new Zero.One.Two.Three.Four.Five().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
      , () -> assertEquals("primaryKey, a, b, c, alternate, toggle", Var.map(Stream.concat(new Zero.One.Two.Three.Four.Five().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), new Zero.One.Two.Three.Four.Five().inspector().getFields().values().stream().map((t) -> new Var.Origin<>(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze)), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
    );
    // @formatter:on

    final Zero.One one = new Zero.One();
    one.abc =new Abc(1, "A", "B", "C");
    assertEquals("0, 0, A, B, C", one.inspector().getFields().values().stream().map((t) -> new Var.Origin<>(one, t)).flatMap(Var.Origin::flatternyze).map(Var::getValue).filter(Objects::nonNull).map(Objects::toString).collect(Collectors.joining(", ")));
  }

}
