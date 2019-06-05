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

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.generic.Inspector;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface Origin {

  /**
   * returns an inspector of this entity .
   *
   * @return an inspector of this entity
   */
  @SuppressWarnings({ "unchecked" })
  default <T extends Origin> Inspector<T> inspector() {
    return Inspector.of((Class<T>) getClass());
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @return the name which converted in the rule of database naming
   */
  default String getName() {
    return StringUtils.defaultIfBlank(inspector().getName(), defaultName());
  }

  /**
   * returns default name if failed converting in the rule of database naming .
   *
   * @return the name which converted in the rule of database naming
   */
  default String defaultName() {
    return getClass().getName().replaceAll("^.*\\.", "").replaceAll("\\<.*$", "");
  }

  /**
   * returns the rule of database naming .
   *
   * @return the rule of database naming
   */
  default NamingType getNamingType() {
    return inspector().getNamingType();
  }

  /**
   * constructing simple SQL query .
   *
   * @param selectBuilder {@link SelectBuilder}
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return selectBuilder ( query structured )
   */
  default SelectBuilder select(SelectBuilder selectBuilder, String... excludeFieldNames) {
    return selectBuilder.sql(selectClause(excludeFieldNames));
  }

  /**
   * returns select clause in SQL query .
   *
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return select clause in SQL query
   */
  default String selectClause(String... excludeFieldNames) {
    return String.format("select %s from %s ", selectColumnNames(excludeFieldNames), getName());
  }

  /**
   * returns database column names defined in this entity .
   *
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return comma-separated database column names
   */
  default String selectColumnNames(String... excludeFieldNames) {
    return "*";
  }
}
