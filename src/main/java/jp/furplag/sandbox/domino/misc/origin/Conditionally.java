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

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.vars.Var;
import jp.furplag.sandbox.domino.misc.vars.Var.Range;
import jp.furplag.sandbox.domino.misc.vars.Where;
import jp.furplag.sandbox.stream.Streamr;
import jp.furplag.sandbox.trebuchet.Trebuchet;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface Conditionally extends Sequentially {

  Map<String, Where<?>> getWheres();

  default <T, ENTITY extends Conditionally> ENTITY where(String fieldName, Where.Operator operator, T value) {
    return where(Trebuchet.Functions.orElse(Trebuchet.Functions.orNot(this, inspector().getField(fieldName), value, Var::varOf), operator, Where::of, (t, u, ex) -> {ex.printStackTrace(); return null; } ));
  }

  @SuppressWarnings("unchecked")
  default <T, ENTITY extends Conditionally> ENTITY where(String fieldName, Where.Operator operator, T... values) {
    return where(Trebuchet.Functions.orElse(Trebuchet.Functions.orNot(this, inspector().getField(fieldName), values, Var::varOf), operator, Where::of, (t, u, ex) -> {ex.printStackTrace(); return null; } ));
  }

  default <T extends Comparable<T>, ENTITY extends Conditionally> ENTITY where(String fieldName, boolean containsEqual, T min, T max) {
    return where(Where.rangeOf((Range<T>) Var.rangeOf(this, inspector().getField(fieldName), min, max), containsEqual));
  }

  @SuppressWarnings({"unchecked"})
  default <T, ENTITY extends Conditionally> ENTITY where(Where<T> where) {
    Trebuchet.Consumers.orNot(where, (_where) -> getWheres().put(_where.getVar().getColumnName(), where));

    return (ENTITY) this;
  }

  /**
   * constructing simple SQL query .
   *
   * @param selectBuilder {@link SelectBuilder}
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return selectBuilder ( query structured )
   */
  default SelectBuilder select(SelectBuilder selectBuilder, String... excludeFieldNames) {
    return select(selectBuilder, new String[] {}, excludeFieldNames);
  }

  /**
   * constructing simple SQL query .
   *
   * @param selectBuilder {@link SelectBuilder}
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return selectBuilder ( query structured )
   */
  default SelectBuilder select(SelectBuilder selectBuilder, String[] excludeSelectFieldNames, String... excludeConditionalFieldNames) {
    return whereClause(selectBuilder.sql(selectClause(excludeSelectFieldNames)), excludeConditionalFieldNames).sql(orderClause());
  }

  /**
   * returns select clause in SQL query .
   *
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return select clause in SQL query
   */
  default SelectBuilder whereClause(SelectBuilder selectBuilder, String... excludeFieldNames) {
    final AtomicReference<String> prefix = new AtomicReference<>(Trebuchet.Predicates.orNot(selectBuilder.getSql().toString(), (sql) -> sql.toLowerCase().replaceAll("\\r?\\n", " ").contains(" where ")) ? "and" : "where");
    // @formatter:off
    getWheres().entrySet().stream()
      .sorted(Comparator.comparing(Map.Entry::getValue))
      .forEach((entry) -> Trebuchet.Consumers.orNot(selectBuilder.sql(String.format(" %s ", prefix.getAndSet("and"))), entry, (_selectBuilder, _entry) -> _entry.getValue().sql(_selectBuilder)));
    // @formatter:on

    return selectBuilder;
  }

  /**
   * returns select clause in SQL query .
   *
   * @param excludeFieldNames field name (s) which excludes from condition
   * @return select clause in SQL query
   */
  default SelectBuilder autoSelect(SelectBuilder selectBuilder, boolean excludeNull) {
    inspector().getFields().stream().map((f) -> Where.of(Var.varOf(this, f), Where.Operator.Equal)).filter((v) -> !excludeNull || !v.getOperator().isNullFinder()).forEach((v) -> where(v));
    Streamr.Filter.filtering(inspector().getFields(), Inspector.Predicates::isIdentity).map(Field::getName).forEach(this::orderBy);

    return select(selectBuilder);
  }
}
