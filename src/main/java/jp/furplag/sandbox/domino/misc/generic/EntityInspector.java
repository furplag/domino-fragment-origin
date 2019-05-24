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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.function.ThrowableFunction;
import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import lombok.Getter;

public class EntityInspector<T extends Origin> extends Inspector<T> {

  /** list of fields which related to a database column . */
  @Getter
  private final Map<String, Field> columnFields;

  /**
   *
   * @param entityClass the class of referenced entity
   * @throws IllegalArgumentException an entity class have to annotate with {@link Entity @Entity}
   */
  public EntityInspector(Class<T> entityClass) {
    super(entityClass);
    columnFields = getColumns(getEntityClass());
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
  private static Map<String, Field> getColumns(final Class<? extends Origin> entityClass) {
    return Streamr.collect(getColumnStream(entityClass).map((t) -> Map.entry(t.getName(), t)), (a, b) -> a, LinkedHashMap::new);
  }

  /**
   * just a internal process for {@link #getColumns(TableNameOrigin)} .
   *
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Stream<Field> getColumnStream(final Class<? extends Origin> entityClass) {
    final AtomicInteger order = new AtomicInteger();
    return Reflections.familyze(entityClass)
      .map((t) -> getColumnsPerClass(order.decrementAndGet(), t))
      .sorted(Comparator.comparing(Map.Entry::getKey))
      .flatMap(Map.Entry::getValue).distinct();
  }

  /**
   * just a internal process for {@link #getColumns(TableNameOrigin)} .
   *
   * @param order an index of element
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Map.Entry<Integer, Stream<Field>> getColumnsPerClass(final int order, final Class<?> entityClass) {
    return Map.entry(order, Streamr.stream(Reflections.getFields(entityClass)).filter(Inspector.isPersistive));
  }

  /**
  * returns nested fields which related to a database column .
  *
  * @param field a member of an entity class, maybe that contains field (s) which related to a database column
  * @return fields which related to a database column
  */
  public static Stream<Field> getActualFields(final Field field) {
    return Streamr.stream(Inspector.isEmbeddable.test(field) ? Reflections.getFields(field.getType()) : null)
      .filter(Inspector.isPersistive);
  }
}
