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

package jp.furplag.sandbox.domino.misc.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.function.ThrowableBiFunction;
import jp.furplag.function.ThrowableBiPredicate;
import jp.furplag.function.ThrowableFunction;
import jp.furplag.function.ThrowablePredicate;
import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.reflect.Reflections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 * @param <T> the type of entity
 */
public abstract class Inspector<T extends Origin> {

  /** tests whether the field annotated with specified {@link Annotation} . */
  private static final BiPredicate<Field, Class<? extends Annotation>> isAnnotated;

  /** tests whether the field annotated with specified {@link Annotation} . */
  private static final BiPredicate<Field, Class<? extends Annotation>> theTypeIsAnnotated;

  /** if the field type is the class which contains field (s) which related to a database column . */
  public static final Predicate<Field> isDomain;

  /** tests if the field type is the class which contains field (s) which related to a database column . */
  public static final Predicate<Field> isEmbeddable;

  /** tests if the field is one of primary key . */
  public static final Predicate<Field> isIdentity;

  /** tests if the field is related to a database column . */
  public static final Predicate<Field> isNotPersistive;

  /** tests if the field is related to a database column . */
  public static final Predicate<Field> isPersistive;

  static {
    isAnnotated = (field, annotation) -> ThrowableBiPredicate.orNot(field, annotation, Reflections::isAnnotatedWith);
    theTypeIsAnnotated = (field, annotation) -> ThrowableBiPredicate.orNot(field, annotation, (t, u) -> Reflections.isAnnotatedWith(t.getType(), u));
    isDomain = (field) -> theTypeIsAnnotated.test(field, Domain.class);
    isEmbeddable = (field) -> theTypeIsAnnotated.test(field, Embeddable.class);
    isIdentity = (field) -> isAnnotated.test(field, Id.class);
    isNotPersistive = Stream.of(Field::isSynthetic, Reflections::isStatic, (Predicate<Field>) (t) -> isAnnotated.test(t, Transient.class)).reduce(Predicate::or).orElse((t) -> true);
    isPersistive = (field) -> ThrowablePredicate.orNot(field, Predicate.not(isNotPersistive)::test);
  }

  /** the class of referenced entity . */
  @NonNull
  @Getter(AccessLevel.PROTECTED)
  private final Class<T> entityClass;

  /** {@link NamingType} which specified this {@link #entityClass} . */
  @NonNull
  @Getter
  private final NamingType namingType;

  /**
   *
   * @param entityClass the class of referenced entity
   * @throws IllegalArgumentException an entity class have to annotate with {@link Entity @Entity}
   */
  protected Inspector(Class<T> entityClass) {
    this.entityClass = Objects.requireNonNull(entityClass);
    this.namingType = getNamingType(Reflections.familyze(entityClass)).orElse(NamingType.NONE);
    Optional.ofNullable(entityClass.getAnnotation(Entity.class)).orElseThrow(IllegalArgumentException::new);
  }

  /**
   * just a internal process for {@link #getNamingType(Object, NamingType)} .
   *
   * @param entityClasses stream of class
   * @return the first result of {@link NamingType NamingType (s) }
   */
  private static Optional<NamingType> getNamingType(final Stream<Class<?>> entityClasses) {
    // @formatter:off
    return entityClasses.map((t) -> ThrowableFunction.orNull(t, (x) -> x.getAnnotation(Entity.class).naming()))
      .filter(Inspector::rejectNone).findFirst();
    // @formatter:on
  }

  /**
   * just a internal process for filtering {@link NamingType} which do nothing .
   *
   * @param namingType {@link NamingType}, maybe null
   * @return true if the namingType equals {@link NamingType#NONE} or null
   */
  private static boolean rejectNone(final NamingType namingType) {
    return !Objects.requireNonNullElse(namingType, NamingType.NONE).equals(NamingType.NONE);
  }

  /**
   * returns the name which annotated with {@link Table#name() @Table(name)} .
   *
   * @return the name specified in Annotation. Or returns null by default
   */
  private final String getAlternate() {
    return StringUtils.defaultIfBlank(ThrowableFunction.orNull(entityClass.getAnnotation(Table.class), Table::name), null);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @return the name which converted in the rule of database naming
   */
  private final String getDefault() {
    return ThrowableBiFunction.orDefault(entityClass, getNamingType(), (t, u) -> u.apply(t.getSimpleName()), entityClass.getSimpleName());
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @return the name which converted in the rule of database naming
   */
  public final String getName() {
    return Optional.ofNullable(getAlternate()).orElse(getDefault());
  }

  /**
   * returns an inspector of defaults .
   *
   * @param <T> the type of entity
   * @param entityClass the class of referenced entity
   * @return {@link Inspector}
   */
  public static <T extends Origin> Inspector<T> defaultInspector(final Class<T> entityClass) {
    return new Inspector<>(entityClass) {};
  }
}
