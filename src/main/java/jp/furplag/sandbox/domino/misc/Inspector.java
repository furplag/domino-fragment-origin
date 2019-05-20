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
package jp.furplag.sandbox.domino.misc;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.function.ThrowableFunction;
import jp.furplag.function.ThrowablePredicate;
import jp.furplag.sandbox.domino.misc.origin.RowOrigin;
import jp.furplag.sandbox.reflect.Reflections;

/**
 * code snippet of generating simple SQL query .
 *
 * @author furplag
 *
 */
public interface Inspector {

  private static String getClassName(final Class<? extends RowOrigin> entityClass) {
    return ThrowableFunction.orNull(entityClass, Class::getSimpleName);
  }

  /**
   * returns the name which annotated with {@link Column#name() Column(name)} .
   *
   * @param field the field maybe a member of an entity
   * @return the name specified in Annotation. Or returns null by default
   */
  private static String getColumnName(final Field field) {
    return StringUtils.defaultIfBlank(ThrowableFunction.orNull(field, (t) -> t.getAnnotation(Column.class).name()), null);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entityClass the class of entity
   * @return the name which converted in the rule of database naming
   */
  private static String getDefaultName(final Class<? extends RowOrigin> entityClass) {
    return ThrowableFunction.orElse(entityClass, (t) -> getNamingType(t).apply(getClassName(t)), (t, e) -> getClassName(t));
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entityClass the class of entity
   * @return the name which converted in the rule of database naming
   */
  private static String getDefaultName(final Class<? extends RowOrigin> entityClass, final Field field) {
    return ThrowableBiFunction.orElse(getNamingType(entityClass), field, (t, u) -> t.apply(u.getName()), (t, u, e) -> u.getName());
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entityClass the class of entity
   * @return the name which converted in the rule of database naming
   */
  static String getName(final Class<? extends RowOrigin> entityClass) {
    return Optional.ofNullable(getTableName(entityClass))
        .orElse(getDefaultName(entityClass));
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param entityClass the class of entity
   * @return the name which converted in the rule of database naming
   */
  static String getName(final Class<? extends RowOrigin> entityClass, Field field) {
    return Optional.ofNullable(getColumnName(field))
        .orElse(getDefaultName(entityClass, field));
  }

  /**
   * just a internal process for {@link #getNamingType(Object, NamingType)} .
   *
   * @param entityClass entity class or instance, maybe that has annotated with {@link Entity @Entity}
   * @return the first result of {@link NamingType NamingType (s) }
   */
  private static NamingType getNamingType(final Class<? extends RowOrigin> entityClass) {
    // @formatter:off
    return Reflections.familyze(entityClass)
      .map((t) -> ThrowableFunction.orNull(t, (x) -> x.getAnnotation(Entity.class).naming()))
      .filter(Predicate.not(NamingType.NONE::equals).and(Objects::nonNull))
      .findFirst().orElse(NamingType.NONE);
    // @formatter:on
  }

  /**
   * returns the name which annotated with {@link Column#name() Column(name)} .
   *
   * @param entityClass an entity
   * @return the name specified in Annotation. Or returns {@link Class#getSimpleName()} by default
   */
  private static String getTableName(final Class<? extends RowOrigin> entityClass) {
    return StringUtils.defaultIfBlank(ThrowableFunction.orNull(entityClass, (t) -> t.getAnnotation(Table.class).name()), null);
  }

  /**
   * inspects if the field type is the class which contains field (s) which related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  static boolean isDomain(Field field) {
    return ThrowablePredicate.orNot(field, (t) -> Reflections.isAnnotatedWith(t.getType(), Domain.class));
  }

  /**
   * inspects if the field type is the class which contains field (s) which related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  static boolean isEmbeddable(Field field) {
    return ThrowablePredicate.orNot(field, (t) -> t.getType().isAnnotationPresent(Embeddable.class));
  }

  /**
   * inspects if the field is one of primary key .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  static boolean isIdentity(Field field) {
    return Reflections.isAnnotatedWith(field, Id.class);
  }

  /**
   * inspects if the field is related to a database column .
   *
   * @param field a member of an entity class
   * @return the result of inspection
   */
  static boolean isPersistive(Field field) {
    return !Reflections.isAnnotatedWith(field, Transient.class);
  }
}
