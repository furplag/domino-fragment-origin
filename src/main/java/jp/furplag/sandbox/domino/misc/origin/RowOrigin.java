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
import java.util.List;

import org.seasar.doma.jdbc.builder.SelectBuilder;

import jp.furplag.function.ThrowableTriConsumer;
import jp.furplag.sandbox.domino.misc.Inspector;
import jp.furplag.sandbox.domino.misc.Retriever;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;
import jp.furplag.sandbox.reflect.Reflections;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface RowOrigin {

  default List<ColumnDef<?>> getColumns() {
    return Retriever.getColumns(this);
  }

  default String getName() {
    return Inspector.getName(getClass());
  }

  default String getName(Field field) {
    return Inspector.getName(getClass(), field);
  }

  default String getName(String fieldName) {
    return Inspector.getName(getClass(), Reflections.getField(this, fieldName));
  }

  default SelectBuilder select(SelectBuilder selectBuilder, ColumnDef<?>... columns) {
    ThrowableTriConsumer.orNot(selectBuilder, Retriever.expand(columns), getName(), (t, u, v) ->  t.sql("select ").sql(u).sql(" from ").sql(v));

    return selectBuilder;
  }
}
