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
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef.ColumnField;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

class EntityOriginTest {

  @Data
  @Entity
  public static class Zero implements EntityOrigin {

    @Data
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

      public Abc aBc;

      @Transient
      public int ignore;

      @Data
      @EqualsAndHashCode(callSuper = true)
      @Entity
      public static class Two extends One {

        @Data
        @EqualsAndHashCode(callSuper = true)
        @Entity(naming = NamingType.SNAKE_LOWER_CASE)
        public static class Three extends Two {

          @Data
          @EqualsAndHashCode(callSuper = true)
          @Entity
          @Table(name = "")
          public static class Four extends Three {
            @Data
            @EqualsAndHashCode(callSuper = true)
            @Entity
            @Table(name = "five_six.se7en")
            public static class Five extends Four {}
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
        () -> assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from TWO", new Zero.One.Two().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select primary_key, rename_this_field, toggle, a, b, C from three", new Zero.One.Two.Three().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select primary_key, rename_this_field, toggle, a, b, C from four", new Zero.One.Two.Three.Four().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select primary_key, rename_this_field, toggle, a, b, C from five_six.se7en", new Zero.One.Two.Three.Four.Five().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
    );
    // @formatter:on
    // @formatter:off
    assertAll(
        () -> assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(TestConfig.singleton()), "one", "two", "three").getSql().toString())
      , () -> assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton()), "one", "two", "three").getSql().toString())
      , () -> assertEquals("select * from ONE", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton()), new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getFieldName).collect(Collectors.joining(", ")).split(", ")).getSql().toString())
      , () -> assertEquals("select PRIMARYKEY from ONE", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton()), new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).filter((t) -> !Inspector.isIdentity.test(t.getField())).map(ColumnDef::getFieldName).collect(Collectors.joining(", ")).split(", ")).getSql().toString())
    );
    // @formatter:on
  }

  @Test
  void paintItGreen() {
    // @formatter:off
    assertAll(
        () -> assertEquals(NamingType.NONE, new Zero().getNamingType())
      , () -> assertEquals(NamingType.UPPER_CASE, new Zero.One().getNamingType())
      , () -> assertEquals(NamingType.UPPER_CASE, new Zero.One.Two().getNamingType())
    );
    // @formatter:on

    // @formatter:off
    assertAll(
        () -> assertEquals("", new Zero().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("PRIMARYKEY, rename_this_field, TOGGLE, A, B, C", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("PRIMARYKEY, rename_this_field, TOGGLE, A, B, C", new Zero.One.Two().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", new Zero.One.Two.Three().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", new Zero.One.Two.Three.Four().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", new Zero.One.Two.Three.Four.Five().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getColumnName).collect(Collectors.joining(", ")))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
          () -> assertEquals("primaryKey", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getField).filter(Inspector.isIdentity).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("toggle", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getField).filter(Inspector.isDomain).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getField).filter(Inspector.isEmbeddable).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getField).filter(Inspector.isNotPersistive).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("primaryKey, alternate, toggle, a, b, c", new Zero.One().getColumns().stream().flatMap(ColumnField::flatten).map(ColumnField::getField).filter(Inspector.isPersistive).map(Field::getName).collect(Collectors.joining(", ")))
        , () -> assertEquals("nope, ignore", Streamr.stream(Reflections.getFields(Zero.One.class)).filter(Inspector.isNotPersistive).filter(Predicate.not(Field::isSynthetic)).map(Field::getName).collect(Collectors.joining(", ")))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
          () -> assertEquals("primaryKey=PRIMARYKEY", new Zero.One().getColumns().stream().findFirst().orElse(null).nameEntry().toString())
        , () -> assertEquals(new Zero.One().getColumns().stream().findFirst().orElse(null).getKey(), new Zero.One().getColumns().stream().findFirst().orElse(null).getFieldName())
        , () -> assertEquals(0L, new Zero.One().getColumns().stream().findFirst().orElse(null).getValue())
        , () -> assertEquals(long.class, new Zero.One().getColumns().stream().findFirst().orElse(null).getValueType())
        , () -> assertEquals(" %s PRIMARYKEY = ", new Zero.One().getColumns().stream().findFirst().orElse(null).getFragment())
        , () -> assertEquals(" %s TOGGLE is ", new Zero.One().getColumns().stream().filter((t) -> "toggle".equals(t.getKey())).findFirst().orElse(null).getFragment())
        , () -> assertThrows(UnsupportedOperationException.class, () -> new ColumnField<>(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).setValue(100L))
        , () -> assertEquals("alternate", new Zero.One().getColumns().stream().sorted(Comparator.reverseOrder()).findFirst().orElse(null).getKey())
        , () -> assertEquals(1, new Zero.One().getColumns().stream().findFirst().orElse(null).compareTo(null))
        , () -> assertEquals(0, new Zero.One().getColumns().stream().findFirst().orElse(null).compareTo(new Zero.One().getColumns().stream().findFirst().orElse(null)))
        , () -> assertEquals(-1, new Zero.One().getColumns().stream().findFirst().orElse(null).compareTo(new Zero.One().getColumns().stream().filter((t) -> "toggle".equals(t.getKey())).findFirst().orElse(null)))
    );
    // @formatter:on

    // @formatter:off
    assertAll(
          () -> assertEquals("select PRIMARYKEY, rename_this_field, TOGGLE, A, B, C from ONE  where PRIMARYKEY = ? and rename_this_field = ? and TOGGLE is ? and A is ? and B is ? and C is ?", ThrowableBiFunction.orNull(new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton())), new Zero.One().getColumns(), (t, u) -> {u.stream().flatMap(ColumnField::flatten).forEach((x) -> x.sql(t)); return t;}).getSql().toString())
    );
    // @formatter:on
  }
}
