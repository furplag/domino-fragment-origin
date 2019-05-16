package jp.furplag.sandbox.domino.misc.vars;

import java.lang.reflect.Field;
import java.util.Map;

import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import jp.furplag.sandbox.reflect.SavageReflection;

/**
 * handles field and database column in entity for generating conditions in simple SQL query .
 *
 * @author furplag
 *
 * @param <V> the type of field
 */
public interface ColumnDef<V> extends Map.Entry<String, V> {

  /**
   * returns {@link Map.Entry} of the name of field and database column .
   *
   * @return the name of the field
   */
  default Map.Entry<String, String> nameEntry() {
    return Map.entry(getFieldName(), getColumnName());
  }

  /**
   * returns an instance which has this field .
   *
   * @return {@link RowOrigin}
   */
  RowOrigin getRowOrigin();

  /**
   * returns the column name of the field .
   *
   * @return the column name of the field
   */
  default String getColumnName() {
    return getRowOrigin().getColumnName(getField());
  }

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

  /** {@inheritDoc}
   * <p>returns the name of the field .</p>
   *
   */
  @Override
  default String getKey() {
    return getFieldName();
  }

  /** {@inheritDoc} */
  @SuppressWarnings({ "unchecked" })
  @Override
  default V getValue() {
    return (V) SavageReflection.get(getRowOrigin(), getField());
  }

  /**
   * returns the type of the field .
   *
   * @return the type of the field
   */
  @SuppressWarnings({ "unchecked" })
  default Class<V> getValueType() {
    return (Class<V>) getField().getType();
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
  default V setValue(V value) {
    throw new UnsupportedOperationException();
  }
}
