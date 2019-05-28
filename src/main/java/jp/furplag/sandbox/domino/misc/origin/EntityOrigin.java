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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import jp.furplag.sandbox.domino.misc.generic.EntityInspector;
import jp.furplag.sandbox.domino.misc.vars.Var;
import jp.furplag.sandbox.stream.Streamr;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface EntityOrigin extends Origin {

  /**
   * returns database columns defined in this entity .
   *
   * @param database columns
   * @param excludeFieldNames field name (s) which excludes from result
   * @return stream of database columns
   */
  private static Stream<Var<?>> filteredColumns(final List<Var<?>> columns, final Set<String> excludeFieldNames) {
    return Streamr.Filter.filtering(columns, (t) -> !excludeFieldNames.contains(t.getFieldName()));
  }

  /**
   * just an internal processfor {@link #getColumns()} .
   *
   * @param database columns
   * @return list of database columns
   */
  private static List<Var<?>> flatternyze(final Stream<Var<?>> columns) {
    return columns.flatMap(Var::flatternyze).sorted().collect(Collectors.toUnmodifiableList());
  }

  /**
   * returns database columns defined in this entity .
   *
   * @return list of database columns
   */
  default List<Var<?>> getColumns() {
    return flatternyze(inspector().getFields().values().stream().map((t) -> new Var.Origin<>(this, t)));
  }

  /** {@inheritDoc} */
  @Override
  default EntityInspector<?> inspector() {
    return new EntityInspector<>(getClass());
  }

  /**
   * returns database column names defined in this entity .
   *
   * @param excludeFieldNames field name (s) which excludes from result
   * @return comma-separated database column names
   */
  @Override
  default String selectColumnNames(String... excludeFieldNames) {
    return StringUtils.defaultIfBlank(filteredColumns(getColumns(), Streamr.collect(HashSet::new, excludeFieldNames)).map(Var::getColumnName).collect(Collectors.joining(", ")), Origin.super.selectColumnNames());
  }
}
