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
import java.util.Objects;

import org.seasar.doma.jdbc.builder.SelectBuilder;

import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import lombok.NonNull;

public interface Where<T> extends Var<T> {

  default String andWhere(SelectBuilder selectBuilder) {
    return selectBuilder.getSql().toString().contains("where") ? "and" : "where";
  }

  @Override
  default SelectBuilder sql(@NonNull SelectBuilder selectBuilder, String fragment) {
    return Objects.nonNull(getValue()) ? injectWhere(selectBuilder) : injectNullWhere(selectBuilder);
  }

  default SelectBuilder injectWhere(@NonNull SelectBuilder selectBuilder) {
    return selectBuilder.sql(String.join(" ", "", andWhere(selectBuilder), getColumnName(), "=", "")).param(getValueType(), getValue());
  }

  default SelectBuilder injectNullWhere(@NonNull SelectBuilder selectBuilder) {
    return selectBuilder.sql(String.join(" ", "", andWhere(selectBuilder), getColumnName(), "is", "null"));
  }

  public static final class Origin<T> extends Var.Origin<T> implements Where<T> {

    public Origin(@NonNull EntityOrigin entity, @NonNull Field field) {
      super(entity, field);
    }
  }

  static <T> Where<T> of(final Var<T> var) {
    return new Where.Origin<>(var.getEntity(), var.getField());
  }
}
