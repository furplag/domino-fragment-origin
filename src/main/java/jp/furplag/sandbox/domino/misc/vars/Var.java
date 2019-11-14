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
import java.util.Objects;
import java.util.stream.Collectors;
import org.seasar.doma.Embeddable;
import com.fasterxml.jackson.annotation.JsonGetter;
import jp.furplag.sandbox.domino.misc.generic.Inspector.Predicates;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.reflect.SavageReflection;
import jp.furplag.sandbox.stream.Streamr;
import jp.furplag.sandbox.trebuchet.Trebuchet;
import jp.furplag.sandbox.tuple.Tag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * handles field and database column in entity for generating conditions in simple SQL .
 *
 * @author furplag
 *
 * @param <T> the type of field
 */
public interface Var<T> extends Comparable<Var<?>>, Tag<String, T> {
  @EqualsAndHashCode(callSuper = true)
  @ToString
  static class AnyOf<T> extends Origin<T> {

    /** conditional values of where clause . */
    @Getter
    private final List<T> values;

    @SafeVarargs
    private AnyOf(jp.furplag.sandbox.domino.misc.origin.Origin entity, Field field, T... values) {
      super(entity, field);
      this.values = Streamr.stream(values).collect(Collectors.toUnmodifiableList());
    }
  }

  @EqualsAndHashCode(of = {"field"})
  @ToString
  static abstract class Origin<T> implements Var<T> {

    /** an instance of {@link jp.furplag.sandbox.domino.misc.origin.Origin}, or the {@link Embeddable} field value . */
    @Getter
    private final jp.furplag.sandbox.domino.misc.origin.Origin entity;

    /** an instance of {@link jp.furplag.sandbox.domino.misc.origin.Origin}, or the {@link Embeddable} field value . */
    private final Object mysterio;

    /** the field in this {@link #entity} which related to database column . */
    @Getter
    @NonNull
    private final Field field;

    /** the column name of {@link #field} . */
    @Getter
    @NonNull
    private final String columnName;

    /**
     *
     * @param entity an instance of {@link jp.furplag.sandbox.domino.misc.origin.Origin}, or the {@link Embeddable} field value
     * @param field the field in this {@link #entity} which related to database column
     */
    private Origin(jp.furplag.sandbox.domino.misc.origin.Origin entity, Field field) {
      this.entity = entity;
      if (Predicates.isDomain(field)) {
        mysterio = SavageReflection.get(entity, field);
        this.field = Reflections.getField(field.getType(), "value");
      } else if (Predicates.isEmbeddableField(field)) {
        mysterio = SavageReflection.get(entity, Streamr.Filter.filtering(Reflections.getFields(entity), (_field) -> _field.getType().equals(field.getDeclaringClass()), (_field) -> Objects.nonNull(Reflections.isAssignable(_field.getType(), field))).findFirst().orElse(null));
        this.field = field;
      } else {
        mysterio = getEntity();
        this.field = field;
      }
      this.columnName = entity.inspector().getName(field);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"unchecked"})
    public T getValue() {
      return (T) SavageReflection.get(mysterio, getField());
    }
  }

  @EqualsAndHashCode(callSuper = true)
  @ToString
  static final class Range<T extends Comparable<T>> extends Origin<T> {

    /**
     * an instance of {@link jp.furplag.sandbox.domino.misc.origin.Origin}, or the {@link Embeddable} field value .
     */
    @Getter
    private final T min;

    /**
     * an instance of {@link jp.furplag.sandbox.domino.misc.origin.Origin}, or the {@link Embeddable} field value .
     */
    @Getter
    private final T max;

    private Range(jp.furplag.sandbox.domino.misc.origin.Origin entity, Field field, T min, T max) {
      super(entity, field);
      this.min = min;
      this.max = max;
    }
  }

  @EqualsAndHashCode(callSuper = true)
  @ToString
  static class Single<T> extends Origin<T> {

    /** conditional value of where clause . */
    @Getter
    private final T value;

    private Single(jp.furplag.sandbox.domino.misc.origin.Origin entity, Field field, T value) {
      super(entity, field);
      this.value = value;
    }
  }

  static <T extends Comparable<T>> Var<T> rangeOf(final jp.furplag.sandbox.domino.misc.origin.Origin entity, final Field field, final T min, final T max) {
    return new Range<>(entity, field, min, max);
  }

  @SafeVarargs
  static <T> Var<T> varOf(final jp.furplag.sandbox.domino.misc.origin.Origin entity, final Field field, final T... values) {
    return Streamr.stream(values).count() > 1 ? new AnyOf<>(entity, field, values) : values == null || values.length < 1 ? new Origin<>(entity, field) {} : new Single<>(entity, field, Trebuchet.Functions.orNot(values, (_values) -> _values[0]));
  }

  /** {@inheritDoc} */
  @Override
  default int compareTo(Var<?> anotherOne) {
    return Integer.compare(Predicates.isIdentity(getField()) ? -1 : 0, Trebuchet.Predicates.orNot(anotherOne, (t) -> Predicates.isIdentity(t.getField())) ? -1 : 0);
  }

  /**
   * <p>
   * returns the name of this field .
   * </p>
   *
   * @return the name of the field
   */
  @JsonGetter
  String getColumnName();

  /**
   * returns an instance of the entity .
   *
   * @return an instance of the entity
   */
  jp.furplag.sandbox.domino.misc.origin.Origin getEntity();

  /**
   * returns the field .
   *
   * @return {@link Field}
   */
  Field getField();

  /**
   * returns the name of this field .
   *
   * @return the name of the field
   */
  default String getFieldName() {
    return getKey();
  }

  /**
   * {@inheritDoc}
   * <p>
   * returns the name of this field .
   * </p>
   *
   * @return the name of the field
   */
  @Override
  default String getKey() {
    return getField().getName();
  }

  /** {@inheritDoc} */
  @JsonGetter
  @Override
  T getValue();

  /**
   * returns the type of the field .
   *
   * @return the type of the field
   */
  @JsonGetter
  @SuppressWarnings({"unchecked"})
  default Class<T> getValueType() {
    return (Class<T>) getField().getType();
  }
}
