package jp.furplag.sandbox.domino.misc;

import java.lang.reflect.Field;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Column;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.function.ThrowableFunction;
import jp.furplag.function.ThrowablePredicate;
import jp.furplag.sandbox.domino.misc.marker.DomainsAware;
import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import jp.furplag.sandbox.reflect.Reflections;

/**
 * code snippet of generating simple SQL query .
 *
 * @author furplag
 *
 */
public interface Inspector {

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entity an entity
   * @param fallbackDefault returns this value if the entity not specified {@link NamingType}
   * @return the name which converted in the rule of database naming
   */
  private static String getTableName(final Object mysterio) {
    return ThrowableFunction.orNull(mysterio, (t) -> StringUtils.defaultIfBlank(t.getClass().getAnnotation(Table.class).name(), t.getClass().getSimpleName()));
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entity an entity
   * @param fallbackDefault returns this value if the entity not specified {@link NamingType}
   * @return the name which converted in the rule of database naming
   */
  private static String getColumnName(final Field field) {
    return ThrowableFunction.orNull(field, (t) -> StringUtils.defaultIfBlank(t.getAnnotation(Column.class).name(), t.getName()));
  }

  /**
   * returns the rule of database naming .
   *
   * @param mysterio entity class or instance, maybe that has annotated with {@link Entity @Entity}
   * @param fallbackDefault returns this value if the entity not specified NamingType. Or returns {@link NamingType.NONE NONE} if that specified null
   * @return {@link NamingType}
   */
  private static NamingType getNamingType(final Object mysterio, final NamingType fallbackDefault) {
    return Reflections.familyze(mysterio).map((t) -> ThrowableFunction.orNull(t, (x) -> x.getAnnotation(Entity.class).naming())).filter(Objects::nonNull).filter(NamingType.NONE::equals).findFirst().orElse(Objects.requireNonNullElse(fallbackDefault, NamingType.NONE));
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entity an entity
   * @return the name which converted in the rule of database naming
   */
  default String getName(RowOrigin entity) {
    return getName(entity, null);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entity an entity
   * @param fallbackDefault returns this value if the entity not specified {@link NamingType}
   * @return the name which converted in the rule of database naming
   */
  default String getName(RowOrigin entity, NamingType fallbackDefault) {
    return ThrowableBiFunction.orNull(getTableName(entity), getNamingType(entity, fallbackDefault), (t, u) -> u.apply(t));
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entity an entity
   * @param fallbackDefault returns this value if the entity not specified {@link NamingType}
   * @return the name which converted in the rule of database naming
   */
  default String getName(RowOrigin entity, Field field, NamingType fallbackDefault) {
    return ThrowableBiFunction.orNull(getColumnName(field), getNamingType(entity, fallbackDefault), (t, u) -> u.apply(t));
  }

  /**
   * inspects if the field type is the class which contains field (s) which related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  default boolean isDomain(Field field) {
    return ThrowablePredicate.orNot(field, (t) -> t.getType().isAnnotationPresent(DomainsAware.class));
  }

  /**
   * inspects if the field type is the class which contains field (s) which related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  default boolean isEmbeddable(Field field) {
    return ThrowablePredicate.orNot(field, (t) -> t.getType().isAnnotationPresent(Embeddable.class));
  }

  /**
   * inspects if the field is related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  default boolean isPersistive(Field field) {
    return !Reflections.isAnnotatedWith(field, Transient.class);
  }

  static Inspector defaultInspector() {
    return new Inspector() {};
  }
}
