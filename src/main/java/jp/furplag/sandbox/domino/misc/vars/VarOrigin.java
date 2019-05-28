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
import java.util.Map;

import jp.furplag.function.ThrowableFunction;
import jp.furplag.sandbox.domino.misc.generic.Inspector;
import jp.furplag.sandbox.domino.misc.origin.EntityOrigin;
import jp.furplag.sandbox.reflect.SavageReflection;

/**
 *
 * @author furplag
 *
 * @param <T> the type of value
 */
public interface VarOrigin<T> extends Comparable<VarOrigin<?>>, Map.Entry<String, T> {

  /**
   * returns the column name of the field .
   *
   * @return the column name of the field
   */
  default String getColumnName() {
    return getEntity().inspector().getName(getField());
  }

  /**
   * returns an instance which has this field .
   *
   * @return {@link EntityOrigin}
   */
  EntityOrigin getEntity();

  /**
   * returns the field .
   *
   * @return {@link Field}
   */
  Field getField();

  /**
   * returns the name of the field .
   *
   * @return the name of the field
   */
  default String getFieldName() {
    return getField().getName();
  }

  /**
   * {@inheritDoc}
   * <p>returns the name of this field .</p>
   *
   * @return the name of the field
   */
  @Override
  default String getKey() {
    return getFieldName();
  }

  /** {@inheritDoc} */
  @SuppressWarnings({ "unchecked" })
  @Override
  default T getValue() {
    return (T) SavageReflection.get(getEntity(), getField());
  }

  /**
   * returns the type of the field .
   *
   * @return the type of the field
   */
  @SuppressWarnings({ "unchecked" })
  default Class<T> getValueType() {
    return (Class<T>) getField().getType();
  }

  /**
   * <p>Throws {@code UnsupportedOperationException} .</p>
   *
   * <p>The {@link ColumnDef} is immutable, so this operation is not supported .</p>
   *
   * @param value  the value to set
   * @return never
   * @throws UnsupportedOperationException as this operation is not supported
   */
  @Override
  default T setValue(T value) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  default int compareTo(VarOrigin<?> anotherOne) {
    return Integer.compare(this.getColumnPriority(), ThrowableFunction.orDefault(anotherOne, VarOrigin::getColumnPriority, -1));
  }

  /**
   * just an internal process for comparing columns .
   *
   * @return the result of comparing by field annotation
   */
  default int getColumnPriority() {
    return Inspector.isIdentity.test(getField()) ? 10 : 20;
  }
}
