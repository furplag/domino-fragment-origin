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

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.function.ThrowableFunction;
import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import lombok.AccessLevel;
import lombok.Getter;

public class EntityInspector<T extends Origin> extends Inspector<T> {

  /** list of a family of this entity . */
  @Getter(AccessLevel.PROTECTED)
  private final List<Class<?>> classes;

  /** list of fields which related to a database column . */
  @Getter
  private final Map<String, Field> fields;

  /**
   *
   * @param entityClass the class of referenced entity
   * @throws IllegalArgumentException an entity class have to annotate with {@link Entity @Entity}
   */
  public EntityInspector(Class<T> entityClass) {
    super(entityClass);
    classes = familyze(entityClass);
    fields = Streamr.collect(getColumns(classes).entrySet().stream().map((t) -> Map.entry(getName(t.getValue()).toLowerCase(Locale.ROOT), t.getValue())), (a, b) -> a, LinkedHashMap::new);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param field a member of the entity
   * @return the name which converted in the rule of database naming
   */
  public final String getName(Field field) {
    return Optional.ofNullable(getAlternate(field)).orElse(getDefault(field));
  }

  /**
   * returns the name which annotated with {@link Column#name() @Column(name)} .
   *
   * @param field a member of the entity
   * @return the name specified in Annotation. Or returns null by default
   */
  private final String getAlternate(Field field) {
    return StringUtils.defaultIfBlank(ThrowableFunction.orNull(field.getAnnotation(Column.class), Column::name), null);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param field a member of the entity
   * @return the name which converted in the rule of database naming
   */
  private final String getDefault(Field field) {
    return ThrowableBiFunction.orDefault(field, getNamingType(), (t, u) -> u.apply(t.getName()), field.getName());
  }

  /**
   * returns fields which related to a database column .
   *
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Map<String, Field> getColumns(final List<Class<?>> family) {
    return Streamr.collect(getColumnStream(family).map((t) -> Map.entry(t.getName(), t)), (a, b) -> a, LinkedHashMap::new);
  }

  /**
   * just a internal process to intializing {@link #classes} .
   *
   * @param entityClass the class of entity
   * @return list of a family of this entity
   */
  private static List<Class<?>> familyze(final Class<?> entityClass) {
    return familyzeInternal(entityClass).sorted(Comparator.comparing(Map.Entry::getKey))
      .map(Map.Entry::getValue).collect(Collectors.toUnmodifiableList());
  }

  /**
   * just a internal process for {@link #familyze(Class)} .
   *
   * @param entityClass the class of entity
   * @return stream of a family of this entity
   */
  private static Stream<Map.Entry<Integer, Class<?>>> familyzeInternal(final Class<?> entityClass) {
    final AtomicInteger order = new AtomicInteger();

    return Reflections.familyze(entityClass).filter(Predicate.not(Object.class::equals)).map((t) -> Map.entry(order.incrementAndGet(), t));
  }

  /**
   * just a internal process for {@link #getColumns(TableNameOrigin)} .
   *
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Stream<Field> getColumnStream(List<Class<?>> family) {
    return family.stream().flatMap(EntityInspector::getColumnsPerClass).distinct();
  }

  /**
   * just a internal process for {@link #getColumns(TableNameOrigin)} .
   *
   * @param order an index of element
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Stream<Field> getColumnsPerClass(final Class<?> entityClass) {
    return Streamr.stream(Reflections.getFields(entityClass)).filter(Inspector.isPersistive);
  }
}
