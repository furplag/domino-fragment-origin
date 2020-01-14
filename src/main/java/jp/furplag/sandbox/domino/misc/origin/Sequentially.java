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
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import jp.furplag.sandbox.trebuchet.Trebuchet;
import jp.furplag.sandbox.tuple.Tag;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

public interface Sequentially extends RowOrigin {

  Queue<OrderBy> getOrder();

  default <ENTITY extends Sequentially> ENTITY orderBy(String fieldName) {
    return orderBy(fieldName, false);
  }

  @SuppressWarnings("unchecked")
  default <ENTITY extends Sequentially> ENTITY orderBy(String fieldName, boolean descendingOrder) {
    Trebuchet.Consumers.orElse(getOrder(), fieldName, descendingOrder, (queue, _fieldName, direction) -> {
      final Field field = Objects.requireNonNullElse(this.inspector().getField(_fieldName), Reflections.getField(this, _fieldName));

      Inspector.Entities.Columns.flatternyze(field).map((_field) -> new OrderBy(inspector().getName(_field), direction)).forEach((x) -> {
        queue.removeIf(x::equals);
        queue.add(x);
      });
    }, (ex) -> {ex.printStackTrace();});

    return (ENTITY) this;
  }

  @SuppressWarnings("unchecked")
  default <ENTITY extends Sequentially> ENTITY orderByExclusive(String fieldName, boolean descendingOrder) {
    Trebuchet.Consumers.orElse(getOrder(), fieldName, descendingOrder, (queue, _fieldName, direction) -> {
      final OrderBy orderBy = new OrderBy(inspector().getNamingType().apply(_fieldName), direction);
      queue.removeIf(orderBy::equals);
      queue.add(orderBy);
    }, (ex) -> {ex.printStackTrace();});

    return (ENTITY) this;
  }

  @EqualsAndHashCode( of = { "columnName" } )
  static final class OrderBy implements Tag<String, Boolean> {

    private final String columnName;

    private final boolean descendingOrder;

    private OrderBy(String columnName) {
      this(columnName, false);
    }

    private OrderBy(@NonNull String columnName, boolean descendingOrder) {
      this.columnName = columnName;
      this.descendingOrder = descendingOrder;
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
      return columnName;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean getValue() {
      return descendingOrder;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.join(" ", columnName, descendingOrder ? "desc" : "").trim();
    }
  }

  @Override
  default SelectBuilder select(SelectBuilder selectBuilder, String... excludeFieldNames) {
    return selectBuilder.sql(selectClause(excludeFieldNames)).sql(orderClause());
  }

  /**
   * returns order clause in SQL query .
   *
   * @return order clause in SQL query
   */
  default String orderClause() {
    final String orderColumn = Streamr.stream(getOrder()).map(OrderBy::toString).collect(Collectors.joining(", "));

    return orderColumn.isBlank() ? "" : String.format(" order by %s ", orderColumn);
  }

}

