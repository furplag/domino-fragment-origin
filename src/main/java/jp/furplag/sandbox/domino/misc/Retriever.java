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
package jp.furplag.sandbox.domino.misc;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef.ColumnField;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.reflect.SavageReflection;
import jp.furplag.sandbox.stream.Streamr;

public interface Retriever {

  /**
   * returns the names of database column for using SQL .
   *
   * @param columnArrays
   * @return database column names
   */
  static String expand(final ColumnDef<?>[]... columnArrays) {
    return StringUtils.defaultIfBlank(columnNames(columnArrays), "*");
  }

  /**
   * returns the names of database column for using SQL .
   *
   * @param columnArrays
   * @return database column names
   */
  static String columnNames(final ColumnDef<?>[]... columnArrays) {
    return flatten(columnArrays).stream().map(ColumnDef::getColumnName).distinct().collect(Collectors.joining(", "));
  }

  /**
   * returns unique columns represented by the type of {@link ColumnDef} .
   *
   * @param columnArrays {@link ColumnDef ColumnDef (s) }, maybe null
   * @return the list of unique columns represented by the type of {@link ColumnDef}
   */
  private static List<ColumnDef<?>> flatten(final ColumnDef<?>[]... columnArrays) {
    return Streamr.stream(columnArrays).flatMap(Streamr::stream).collect(Collectors.toUnmodifiableList());
  }

  /**
   * returns nested fields which related to a database column .
   *
   * @param entity an entity
   * @param field a member of an entity class, maybe that contains field (s) which related to a database column
   * @return fields which related to a database column
   */
  static Field[] getActualFields(final RowOrigin entity, final Field field) {
    return Streamr.Filter.filtering(Reflections.getFields(SavageReflection.get(entity, field)), Inspector::isPersistive).toArray(Field[]::new);
  }

  /**
   * returns fields which related to a database column .
   *
   * @param entity an entity
   * @return fields which related to a database column
   */
  static List<ColumnDef<?>> getColumns(final RowOrigin entity) {
    return getColumnStream(entity)
      .map((t) -> new ColumnField<>(entity, t)).collect(Collectors.toUnmodifiableList());
  }

  /**
   * just a internal process for {@link #getColumns(RowOrigin)} .
   *
   * @param entity an entity
   * @return fields which related to a database column
   */
  private static Stream<Field> getColumnStream(final RowOrigin entity) {
    final AtomicInteger order = new AtomicInteger();
    return Reflections.familyze(entity)
      .map((t) -> getColumnsPerClass(order.decrementAndGet(), t))
      .sorted(Comparator.comparing(Map.Entry::getKey))
      .flatMap(Map.Entry::getValue).map(Reflections::conciliation).distinct().filter(Predicate.not(Field::isSynthetic));
  }

  /**
   * just a internal process for {@link #getColumns(RowOrigin)} .
   *
   * @param order an index of element
   * @param entity an entity
   * @return fields which related to a database column
   */
  static Map.Entry<Integer, Stream<Field>> getColumnsPerClass(final int order, final Class<?> entityClass) {
    return Map.entry(order, Streamr.stream(Reflections.getFields(entityClass)).filter(Inspector::isPersistive));
  }


}
