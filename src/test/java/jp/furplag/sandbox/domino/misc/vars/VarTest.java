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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
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
import org.seasar.doma.jdbc.entity.NamingType;
import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.origin.Conditionally;
import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import jp.furplag.sandbox.domino.misc.origin.Sequentially;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import jp.furplag.sandbox.trebuchet.Trebuchet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

class VarTest {

  public static final org.seasar.doma.jdbc.Config config = TestConfig.singleton();

  @Entity
  public static class Zero implements RowOrigin {

    @EqualsAndHashCode(callSuper = true)
    @Entity(naming = NamingType.UPPER_CASE)
    public static class One extends Zero implements Conditionally {

      @Transient
      @Getter
      Map<String, Where<?>> wheres = new LinkedHashMap<>();

      @Transient
      @Getter
      Queue<Sequentially.OrderBy> order = new ConcurrentLinkedQueue<>();

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
            @Table(name = "five_six.se7en")
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
  public static class Abc implements Comparable<Abc> {
    @Transient
    public int ignore;

    public String a;

    public String b;

    @Column(name = "C")
    public String c;

    public Abc(String a, String b, String c) {
      this(0, a, b, c);
    }

    @Override
    public int compareTo(Abc o) {
      return Integer.compare(ignore, Trebuchet.Functions.orElse(o, Abc::getIgnore, () -> ignore - 1));
    }
  }

  @Test
  void test() {
    assertEquals(0L, Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).getValue());
    assertEquals(long.class, Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).getValueType());
    assertEquals(new Zero.One().abc, Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "abc")).getValue());
    assertEquals(String.class, Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getValueType());
    assertEquals(Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getFieldName(), Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "toggle")).getKey());
  }

  @Test
  void testEntity() {}
  //
  // @Test
  // void paintItGreen() {
//    // @formatter:off
//    assertAll(
//        () -> assertThrows(NullPointerException.class, () -> Var.varOf(null, null))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(null, Reflections.getField(Zero.One.class, "primaryKey")))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "notExists")))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(null, null))
//      , () -> assertThrows(UnsupportedOperationException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).setValue(123L))
//      , () -> assertEquals("PRIMARYKEY", Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(SelectBuilder.newInstance(TestConfig.singleton()), null).getSql().toString().replaceAll("\\r\\n", ""))
//      , () -> assertEquals("PRIMARYKEY", new Zero.One().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted().map(Var::getColumnName).findFirst().orElse(null))
//      , () -> assertEquals("rename_this_field", new Zero.One().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted(Comparator.reverseOrder()).map(Var::getColumnName).findFirst().orElse(null))
//    );
//    // @formatter:on
  //
//    // @formatter:off
//    assertAll(
//        () -> assertThrows(NullPointerException.class, () -> Var.varOf(null, null))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(null, Reflections.getField(Zero.One.class, "primaryKey")))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "notExists")))
//      , () -> assertThrows(NullPointerException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(null, null))
//      , () -> assertThrows(UnsupportedOperationException.class, () -> Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).setValue(123L))
//      , () -> assertEquals("PRIMARYKEY", Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "primaryKey")).sql(SelectBuilder.newInstance(TestConfig.singleton()), null).getSql().toString().replaceAll("\\r\\n", ""))
//      , () -> assertEquals("PRIMARYKEY", new Zero.One().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One(), t)).flatMap(Var.Origin::flatternyze).sorted().map(Var::getColumnName).findFirst().orElse(null))
//      , () -> assertEquals("primary_key, rename_this_field, toggle, a, b, C", Var.map(new Zero.One.Two.Three().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Var::getColumnName).keySet().stream().collect(Collectors.joining(", ")))
//      , () -> assertEquals("primaryKey, alternate, toggle, a, b, c", Var.map(new Zero.One.Two.Three().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
//      , () -> assertEquals("primary_key, alternate, rename_this_field, toggle, a, b, C", Var.map(new Zero.One.Two.Three.Four().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Var::getColumnName).keySet().stream().collect(Collectors.joining(", ")))
//      , () -> assertEquals("primaryKey, a, b, c, alternate, toggle", Var.map(new Zero.One.Two.Three.Four.Five().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
//      , () -> assertEquals("primaryKey, a, b, c, alternate, toggle", Var.map(Stream.concat(new Zero.One.Two.Three.Four.Five().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze), new Zero.One.Two.Three.Four.Five().inspector().getFields().stream().map((t) -> Var.varOf(new Zero.One.Two.Three(), t)).flatMap(Var.Origin::flatternyze)), Map.Entry::getKey).keySet().stream().collect(Collectors.joining(", ")))
//    );
//    // @formatter:on
  // }

  public static void main(String[] args) {

    new Zero.One().inspector().getFields().stream().map((f) -> Var.varOf(new Zero.One(), f)).forEach((v) -> {
      System.out.print(v.getFieldName());
      System.out.print(" : ");
      System.out.print(v.getColumnName());
      System.out.print(" : ");
      System.out.print(v.getValue());
      System.out.print(" : ");
      System.out.println(v.getValueType().getSimpleName());
    });
    System.out.println("-- --");
    new Zero.One.Two.Three.Four.Five().inspector().getFields().stream().map((f) -> Var.varOf(new Zero.One.Two.Three.Four.Five(), f)).forEach((v) -> {
      System.out.print(v.getFieldName());
      System.out.print(" : ");
      System.out.print(v.getColumnName());
      System.out.print(" : ");
      System.out.print(v.getValue());
      System.out.print(" : ");
      System.out.println(v.getValueType().getSimpleName());
    });
    Streamr.stream(Var.varOf(new Zero.One(), Reflections.getField(Zero.One.class, "abc"))).forEach((v) -> {
      System.out.print(v.getFieldName());
      System.out.print(" : ");
      System.out.print(v.getColumnName());
      System.out.print(" : ");
      // System.out.print(v.getValue());
      // System.out.print(" : ");
      System.out.println(v.getValueType().getSimpleName());
    });

    System.out.println(new Zero.One().inspector().getField("abc"));
  }
}
