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

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.seasar.doma.jdbc.builder.SelectBuilder;

import jp.furplag.function.ThrowableFunction;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.reflect.SavageReflection;
import jp.furplag.sandbox.stream.Streamr;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * handles field and database column in entity for generating conditions in simple SQL .
 *
 * @author furplag
 *
 * @param <T> the type of field
 */
public interface Var<T> extends Comparable<Var<?>>, Map.Entry<String, T> {

  /**
   * handles field and database column in entity for generating conditions in simple SQL .
   *
   * @author furplag
   *
   * @param <T> the type of field
   */
  @EqualsAndHashCode(of = { "entity", "field" })
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Embed<T> implements Var<T> {

    /** instance of entity which related to database row . */
    @NonNull
    @Getter
    private final EntityOrigin entity;

    /** field value of {@link org.seasar.doma.Embeddable @Embeddable} . */
    @Getter
    private final Object mysterio;

    /** the field actually related to database column . */
    @NonNull
    @Getter
    private final Field field;

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked" })
    @Override
    public T getValue() {
      return (T) SavageReflection.get(mysterio, field);
    }
  }

  /**
   * handles field and database column in entity for generating conditions in simple SQL .
   *
   * @author furplag
   *
   * @param <T> the type of field
   */
  @EqualsAndHashCode(of = { "entity", "field" })
  public static class Origin<T> implements Var<T> {

    /** instance of entity which related to database row . */
    @Getter
    private final EntityOrigin entity;

    /** the field in this {@link #entity} which related to database column . */
    @Getter
    private final Field field;

    /** nested field of this {@link #entity} . */
    @Getter
    private final List<Var<?>> actualFields;

    /**
     *
     *
     * @param entity instance of entity which related to database row, may not be null
     * @param field he field in this {@link #entity} which related to database column, may not be null
     */
    public Origin(@NonNull EntityOrigin entity, @NonNull Field field) {
      this.entity = entity;
      this.field = field;
      actualFields = getActualFields(field).map((t) -> new Embed<>(entity, SavageReflection.get(entity, field), t)).collect(Collectors.toUnmodifiableList());
    }

    /**
     * returns the field (s) actually related to database column .
     *
     * @param field the field in this {@link #entity}, maybe contains field (s) which related to a database column
     * @return the field (s) actually related to database column
     */
    private static Stream<Field> getActualFields(final Field field) {
      return Inspector.Predicates.isEmbeddable(field) ? Streamr.Filter.filtering(Reflections.getFields(field.getType()), Inspector.Predicates::isPersistive) :
        Stream.empty();
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<Var<?>> flatternyze() {
      return getActualFields().isEmpty() ? Var.super.flatternyze() : Streamr.stream(getActualFields());
    }
  }

  /** {@inheritDoc} */
  @Override
  default int compareTo(Var<?> anotherOne) {
    return Integer.compare(this.getColumnPriority(), ThrowableFunction.orDefault(anotherOne, Var::getColumnPriority, -1));
  }

  /**
   * returns the column name of the field .
   *
   * @return the column name of the field
   */
  default String getColumnName() {
    return getEntity().inspector().getName(getField());
  }

  /**
   * just an internal process for comparing columns .
   *
   * @return the result of comparing by field annotation
   */
  default int getColumnPriority() {
    return Inspector.Predicates.isIdentity(getField()) ? 10 : 20;
  }

  /**
   * returns an instance which has this field .
   *
   * @return {@link EntityOrigin}
   */
  EntityOrigin getEntity();

  /**
   * returns the field .
   *
   * @return {@link Field}
   */
  Field getField();

  /**
   * returns the name of the field .
   *
   * @return the name of the field
   */
  default String getFieldName() {
    return getField().getName();
  }

  /**
   * {@inheritDoc}
   * <p>returns the name of this field .</p>
   *
   * @return the name of the field
   */
  @Override
  default String getKey() {
    return getFieldName();
  }

  /** {@inheritDoc} */
  @SuppressWarnings({ "unchecked" })
  @Override
  default T getValue() {
    return (T) SavageReflection.get(getEntity(), getField());
  }

  /**
   * returns the type of the field .
   *
   * @return the type of the field
   */
  @SuppressWarnings({ "unchecked" })
  default Class<T> getValueType() {
    return (Class<T>) getField().getType();
  }

  /**
   * <p>Throws {@code UnsupportedOperationException} .</p>
   *
   * <p>The {@link Var} is immutable, so this operation is not supported .</p>
   *
   * @param value  the value to set
   * @return never
   * @throws UnsupportedOperationException as this operation is not supported
   */
  @Override
  default T setValue(T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * constructing simple SQL .
   *
   * @param selectBuilder {@link SelectBuilder}
   * @param fragment  a fragment of SQL query, e.g comma, space, &quot;where&quot;, and &quot;and&quot;
   * @return selectBuilder ( query structured )
   */
  default SelectBuilder sql(@NonNull SelectBuilder selectBuilder, String fragment) {
    return selectBuilder.sql(String.join("", Objects.toString(fragment, " "), getColumnName()));
  }

  /**
   * shorthand for {@link Collectors#toMap(Function, Function)} .
   *
   * @param vars stream of fields
   * @param keyMapper a mapping function to produce keys
   * @return {@link Map} of {@link Var}
   */
  static Map<String, Var<?>> map(final Stream<Var<?>> vars, final Function<Var<?>, String> keyMapper) {
    return Streamr.stream(vars).sorted().collect(Collectors.toMap(keyMapper, (v) -> v, (a, b) -> a, LinkedHashMap::new));
  }

  /**
   * returns the field (s) actually related to database column .
   *
   * @return the field (s) actually related to database column
   */
  default Stream<Var<?>> flatternyze() {
    return Stream.of(this);
  }
}
