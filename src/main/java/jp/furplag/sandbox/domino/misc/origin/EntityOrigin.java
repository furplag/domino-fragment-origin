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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import jp.furplag.sandbox.stream.Streamr;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface EntityOrigin extends Origin {

  /**
   * returns database column names defined in this entity .
   *
   * @param excludeFieldNames field name (s) which excludes from result
   * @return comma-separated database column names
   */
  @Override
  default String selectColumnNames(String... excludeFieldNames) {
    return StringUtils.defaultIfBlank(toString(getFields(inspector().getFields(), excludes(excludeFieldNames)), inspector()::getName), Origin.super.selectColumnNames());
  }

  /**
   * returns database columns defined in this entity .
   *
   * @param columns database columns
   * @param excludeFieldNames field name (s) which excludes from result
   * @return stream of database columns
   */
  private static Stream<Field> getFields(final List<Field> fields, final Set<String> excludeFieldNames) {
    return Streamr.Filter.filtering(fields, (t) -> !excludeFieldNames.contains(t.getName()));
  }

  private static Set<String> excludes(final String... excludeFieldNames) {
    return Streamr.stream(excludeFieldNames).collect(Collectors.toUnmodifiableSet());
  }


  private static <T> String toString(final Stream<T> stream, final Function<T, String> toString) {
    return Streamr.stream(stream).map(toString).collect(Collectors.joining(", "));
  }
}
