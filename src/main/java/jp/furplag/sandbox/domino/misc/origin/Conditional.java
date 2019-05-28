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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.seasar.doma.jdbc.builder.SelectBuilder;

import jp.furplag.sandbox.domino.misc.vars.Var;
import jp.furplag.sandbox.domino.misc.vars.Where;
import jp.furplag.sandbox.stream.Streamr;

public interface Conditional extends EntityOrigin {

  Map<String, Where<?>> getWheres();

  default Conditional where(Stream<Var<?>> vars) {
    Streamr.stream(vars).forEach((var) -> getWheres().put(var.getColumnName(), Where.of(var)));

    return this;
  }

  @Override
  default SelectBuilder select(SelectBuilder selectBuilder, String... excludeFieldNames) {
    EntityOrigin.super.select(selectBuilder);
    final Set<String> excludes = Streamr.collect(HashSet::new, excludeFieldNames);
    getWheres().values().stream().filter((t) -> !excludes.contains(t.getFieldName())).sorted().forEach((t) -> t.sql(selectBuilder, ""));

    return selectBuilder;
  }
}
