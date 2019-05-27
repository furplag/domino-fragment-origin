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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import lombok.Getter;

/**
 * handles field and database column in entity for generating conditions in simple SQL query .
 *
 * @author furplag
 *
 * @param <T> the type of field
 */
public class ColumnDef<T> implements VarOrigin<T> {

  /** instance of entity . */
  @Getter
  private final EntityOrigin entity;

  /** field of entity . */
  @Getter
  private final Field field;

  /** nested field of entity . */
  @Getter
  private final List<ColumnDef<?>> actualFields;

  /**
   *
   * @param entity an entry
   * @param field of entity
   */
  public ColumnDef(EntityOrigin entity, Field field) {
    this.entity = Objects.requireNonNull(entity);
    this.field = Objects.requireNonNull(field);
    actualFields = Streamr.Filter.filtering(Reflections.getFields(Inspector.isEmbeddable.test(getField()) ? getValueType() : null), Inspector.isPersistive::test)
      .map((t) -> new ColumnDef<>(entity, t)).collect(Collectors.toUnmodifiableList());
  }

  /**
   * returns actual field (s) if the type of this field is {@link org.seasar.doma.Embeddable @Embeddable} .
   *
   * @return nested field (s)
   */
  public final Stream<ColumnDef<?>> flatternyze() {
    return actualFields.isEmpty() ? Stream.of(this) : Streamr.stream(actualFields);
  }
}
