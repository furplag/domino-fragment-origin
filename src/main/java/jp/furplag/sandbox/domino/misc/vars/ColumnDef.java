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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.seasar.doma.jdbc.builder.SelectBuilder;

import jp.furplag.function.ThrowableTriFunction;
import jp.furplag.sandbox.domino.misc.generic.EntityInspector;
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.reflect.SavageReflection;
import lombok.Getter;

/**
 * handles field and database column in entity for generating conditions in simple SQL query .
 *
 * @author furplag
 *
 * @param <T> the type of field
 */
public interface ColumnDef<T> extends Comparable<ColumnDef<?>>, Map.Entry<String, T> {

  /**
   * returns {@link Map.Entry} of the name of field and database column .
   *
   * @return the name of the field
   */
  default Map.Entry<String, String> nameEntry() {
    return Map.entry(getFieldName(), getColumnName());
  }

  /**
   * returns an instance which has this field .
   *
   * @return {@link EntityOrigin}
   */
  EntityOrigin getEntity();

  /**
   * returns the column name of the field .
   *
   * @return the column name of the field
   */
  default String getColumnName() {
    return getEntity().inspector().getName(getField());
  }

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
   * returns the name of the field .
   *
   * @return the name of the field
   */
  default String getFragment() {
    return new StringJoiner(" ", " %s ", " ").add(getColumnName()).add(Objects.nonNull(getValue()) ? "=" : "is").toString();
  }

  /** {@inheritDoc}
   * <p>returns the name of the field .</p>
   *
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
   * <p>The {@link ColumnDef} is immutable, so this operation is not supported .</p>
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
   * @return selectBuilder ( query structured )
   */
  default SelectBuilder sql(SelectBuilder selectBuilder) {
    return ThrowableTriFunction.orDefault(selectBuilder, getFragment(), andWhere(selectBuilder)
      , (t, u, v) -> {t.sql(String.format(u, v.getAndSet("and"))).param(getValueType(), getValue()); return t;}, selectBuilder);
  }

  private AtomicReference<String> andWhere(final SelectBuilder selectBuilder) {
    return new AtomicReference<>(selectBuilder.getSql().toString().contains("where ") ? "and" : "where");
  }

  /** {@inheritDoc} */
  @Override
  default int compareTo(ColumnDef<?> anotherOne) {
    return anotherOne == null ? 1 : prior().compareTo(anotherOne.prior());
  }

  default Integer prior() {
    return (EntityInspector.isIdentity.test(getField()) ? 0 : 1);
  }

  static class ColumnField<T> implements ColumnDef<T> {

    @Getter
    private final EntityOrigin entity;

    @Getter
    private final Field field;

    @Getter
    private final List<ColumnField<?>> actualFields;

    public ColumnField(EntityOrigin entity, Field field) {
      this.entity = Objects.requireNonNull(entity);
      this.field = Objects.requireNonNull(field);
      actualFields = EntityInspector.getActualFields(field)
        .map((actualField) -> new ColumnField<>(entity, actualField)).collect(Collectors.toUnmodifiableList());
    }

    public final Stream<ColumnField<?>> flatten() {
      return actualFields.isEmpty() ? Stream.of(this) : actualFields.stream();
    }
  }
}
