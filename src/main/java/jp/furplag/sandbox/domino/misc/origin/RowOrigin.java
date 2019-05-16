package jp.furplag.sandbox.domino.misc.origin;


import java.lang.reflect.Field;
import java.util.Map;

import jp.furplag.sandbox.domino.misc.Inspector;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;
import jp.furplag.sandbox.reflect.Reflections;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 */
public interface RowOrigin {

  Map<String, ColumnDef<?>> getColumns();

  default String getTableName() {
    return Inspector.defaultInspector().getName(this);
  }

  default String getColumnName(Field field) {
    return Inspector.defaultInspector().getName(this, field, null);
  }

  default String getColumnName(String fieldName) {
    return Inspector.defaultInspector().getName(this, Reflections.getField(this, fieldName), null);
  }
}
