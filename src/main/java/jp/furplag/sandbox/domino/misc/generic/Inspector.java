/**
 * Copyright (C) 2019+ furplag (https://github.com/furplag)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package jp.furplag.sandbox.domino.misc.generic;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.Column;
import org.seasar.doma.Domain;
import org.seasar.doma.Embeddable;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.entity.NamingType;
import jp.furplag.sandbox.domino.misc.DomainsAware;
import jp.furplag.sandbox.domino.misc.origin.Origin;
import jp.furplag.sandbox.reflect.Reflections;
import jp.furplag.sandbox.stream.Streamr;
import jp.furplag.sandbox.trebuchet.Trebuchet;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * a simply structure of the {@link org.seasar.doma.Entity} .
 *
 * @author furplag
 *
 * @param <ENTITY> the type of entity
 */
@EqualsAndHashCode(of = {"classes", "fields", "namingType"})
@ToString(of = {"entityClass"})
public class Inspector<ENTITY extends Origin> {

  /**
   * a simply structure of the {@link org.seasar.doma.Entity} .
   *
   * @author furplag
   *
   */
  public static interface Entities {

    /**
     * a simply structure of the {@link org.seasar.doma.Entity} .
     *
     * @author furplag
     *
     */
    public static interface Columns {

      /**
       * returns stream of unique values merged into values with same keys represented by keyMapper .
       *
       * @param fields stream of field
       * @param keyMapper a function to merging values
       * @param overWrite if true, overwrite the value when key duplicated
       * @return stream of field
       */
      private static <T> Stream<Field> distinct(final Stream<Field> fields, final Function<Field, T> keyMapper) {
        return Streamr.collect(Streamr.stream(fields).map((field) -> Map.entry(keyMapper.apply(field), field)), (a, b) -> a, LinkedHashMap::new).values().stream();
      }

      /**
       * returns the field (s) actually related to database column .
       *
       * @param field a member of the entity
       * @return the field (s) actually related to database column
       */
      static Stream<Field> flatternyze(final Field field) {
        return Predicates.isEmbeddable(field) ? Streamr.Filter.filtering(Reflections.getFields(field.getType()), Predicates::isPersistive) : Stream.of(field);
      }

      /**
       * tests if the field matches any of values .
       *
       * @param field a member of the entity
       * @param condition a condition for matching, eg. {@link Field#getName()}
       * @param values values for matching, they must be implemets {@link #equals(Object)}
       * @return true if the field matches any of values
       */
      @SafeVarargs
      private static <T> boolean matches(final Field field, final Function<Field, ? extends T> condition, final T... values) {
        return Streamr.stream(values).collect(Collectors.toSet()).contains(condition.apply(field));
      }
    }

    /**
     * a simply structure of the {@link org.seasar.doma.Entity} .
     *
     * @author furplag
     *
     */
    public static interface Names {

      /**
       * returns the name which annotated with {@link Table#name() @Table(name)} .
       *
       * @param entityClass the class of entity
       * @return the name specified in Annotation. Or returns null by default
       */
      private static String getAlternate(final Class<? extends Origin> entityClass) {
        return StringUtils.defaultIfBlank(Trebuchet.Functions.orNot(entityClass.getAnnotation(Table.class), Table::name), null);
      }

      /**
       * returns the name which annotated with {@link Column#name() @Column(name)} .
       *
       * @param field a member of the entity
       * @return the name specified in Annotation. Or returns null by default
       */
      private static String getAlternate(final Field field) {
        return StringUtils.defaultIfBlank(Trebuchet.Functions.orNot(field.getAnnotation(Column.class), Column::name), null);
      }

      /**
       * returns the name which converted in the rule of database naming .
       *
       * @param entityClass the class of entity
       * @return the name which converted in the rule of database naming
       */
      private static String getDefault(final Class<? extends Origin> entityClass) {
        return Trebuchet.Functions.orNot(entityClass, getNamingType(entityClass), (t, u) -> u.apply(t.getSimpleName()));
      }

      /**
       * returns the name which converted in the rule of database naming .
       *
       * @param field a member of the entity
       * @param namingType the rule of database naming
       * @return the name which converted in the rule of database naming
       */
      private static String getDefault(final Field field, final NamingType namingType) {
        return Trebuchet.Functions.orElse(field, namingType, (t, u) -> u.apply(t.getName()), (t, u, ex) -> t.getName());
      }

      /**
       * returns the name which converted in the rule of database naming .
       *
       * @param entityClass the class of entity
       * @return the name which converted in the rule of database naming
       */
      static String getName(final Class<? extends Origin> entityClass) {
        return StringUtils.defaultIfBlank(getAlternate(entityClass), getDefault(entityClass));
      }

      /**
       * returns the name which converted in the rule of database naming .
       *
       * @param field a member of the entity
       * @param namingType the rule of database naming
       * @return the name which converted in the rule of database naming
       */
      static String getName(final Field field, final NamingType namingType) {
        return StringUtils.defaultIfBlank(getAlternate(field), getDefault(field, namingType));
      }

      /**
       * returns {@link NamingType} of specified with {@link org.seasar.doma.Entity#naming() @Entity#naming()} in entity class or parents .
       *
       * @param entityClass the class of entity
       * @return the first result of {@link NamingType NamingType (s) }
       */
      static NamingType getNamingType(final Class<? extends Origin> entityClass) {
        return getNamingType(familyze(entityClass).toArray(Class<?>[]::new)).orElse(NamingType.NONE);
      }

      /**
       * returns {@link NamingType} of specified with {@link org.seasar.doma.Entity#naming() @Entity#naming()} in entity class or parents .
       *
       * @param classes the type of an entity and parents of
       * @return the first result of {@link NamingType NamingType (s) }, if exists
       */
      private static Optional<NamingType> getNamingType(final Class<?>... classes) {
        // @formatter:off
        return Streamr.Filter.filtering(classes, Entities.Names::rejectNone)
          .map((t) -> t.getAnnotation(org.seasar.doma.Entity.class).naming()).findFirst();
        // @formatter:on
      }

      /**
       * tests if an entity class specified any of {@link NamingType} .
       *
       * @param entityClass the class of entity, maybe has specified a {@link NamingType}
       * @return true if the namingType is null
       */
      private static boolean rejectNone(final Class<?> entityClass) {

        return !NamingType.NONE.equals(Trebuchet.Functions.orNot(entityClass, (t) -> t.getAnnotation(org.seasar.doma.Entity.class).naming()));
      }
    }

    /**
     * just a converting array to {@link Set} .
     *
     * @param <T> the type of variable
     * @param excludes the elements
     * @return unique collection of the elements
     */
    @SafeVarargs
    static <T> Set<T> excludes(final T... excludes) {
      return Streamr.stream(excludes).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * returns list of a family of this entity .
     *
     * @param <T> the type of entity
     * @param entityClass the class of entity
     * @return list of a family of this entity
     */
    static <T extends Origin> List<Class<?>> familyze(final Class<T> entityClass) {
      // @formatter:off
      return familyzeInternal(entityClass).sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .collect(Collectors.toUnmodifiableList());
      // @formatter:on
    }

    /**
     * just a internal process for {@link #familyze(Class)} .
     *
     * @param <T> the type of entity
     * @param entityClass the class of entity
     * @return stream of a family of this entity
     */
    private static <T extends Origin> Stream<Map.Entry<Integer, Class<?>>> familyzeInternal(final Class<T> entityClass) {
      final AtomicInteger order = new AtomicInteger();
      // @formatter:off
      return Reflections.familyze(entityClass).filter(Entities::isEntity)
        .map((t) -> Map.entry(order.incrementAndGet(), t));
      // @formatter:on
    }

    /**
     * returns fields which related to a database column .
     *
     * @param classes the type of an entity and parents of
     * @return fields which related to a database column, maybe duplicates
     */
    private static Stream<Field> getAllColumnFields(final Class<?>... classes) {
      return Streamr.stream(classes).map(Reflections::getFields).flatMap(Streamr::stream).flatMap(Columns::flatternyze).filter(Predicates::isPersistive);
    }

    /**
     * returns fields which related to a database column .
     *
     * @param classes the type of an entity and parents of
     * @param condition a condition for exclusion, eg. {@link Field#getName()}
     * @param excludeConditions values for exclusion, they must be implemets {@link #equals(Object)}
     * @return fields which related to a database column
     */
    @SafeVarargs
    private static <T> List<Field> getColumnFields(final Class<?>[] classes, final Function<Field, ? extends T> condition, T... excludeConditions) {
      return getUniqueColumnFields(classes, condition, excludeConditions).sorted(Comparator.comparing((field) -> Predicates.isIdentity(field) ? -1 : 0)).collect(Collectors.toUnmodifiableList());
    }


    /**
     * returns fields which related to a database column .
     *
     * @param classes the type of an entity and parents of
     * @param condition a condition for exclusion, eg. {@link Field#getName()}
     * @param excludeConditions values for exclusion, they must be implemets {@link #equals(Object)}
     * @return fields which related to a database column
     */
    @SafeVarargs
    private static <T> Stream<Field> getUniqueColumnFields(final Class<?>[] classes, final Function<Field, ? extends T> condition, T... excludeConditions) {
      return Columns.distinct(getAllColumnFields(classes).filter((field) -> !Columns.matches(field, condition, excludeConditions)), condition);
    }

    /**
     * tests whether the object annotated with {@link org.seasar.doma.Entity @Entity} .
     *
     * @param mysterio the class or an instance of any entity
     * @return true if the object is an entity
     */
    static boolean isEntity(final Object mysterio) {
      return Reflections.isAnnotatedWith(Reflections.getClass(mysterio), org.seasar.doma.Entity.class);
    }
  }

  /**
   * a simply structure of the {@link org.seasar.doma.Entity} .
   *
   * @author furplag
   *
   */
  public static interface Predicates {

    /**
     * tests whether the type of a field annotated with specified {@link Annotation} .
     *
     * @param field a member of the entity
     * @param annotation any of {@link Annotation}
     * @return true if the type of a field annotated with specified {@link Annotation}
     */
    static boolean fieldTypeIsAnnotated(final Field field, Class<? extends Annotation> annotation) {
      return Trebuchet.Predicates.orNot(field, annotation, (t, u) -> Reflections.isAnnotatedWith(t.getType(), u));
    }

    /**
     * tests whether the field annotated with specified {@link Annotation} .
     *
     * @param mysterio the type of an entity, or field of the entity
     * @param annotation any of {@link Annotation}
     * @return true if the field annotated with specified {@link Annotation}
     */
    static boolean isAnnotated(AccessibleObject mysterio, Class<? extends Annotation> annotation) {
      return Trebuchet.Predicates.orNot(mysterio, annotation, Reflections::isAnnotatedWith);
    }

    /**
     * tests if the field type is {@link Domain @Domain} in DOMA .
     *
     * @param field a member of the entity
     * @return true if the field type is {@link Domain @Domain} in DOMA
     */
    static boolean isDomain(final Field field) {
      return Stream.of(Domain.class, DomainsAware.class).anyMatch(field.getType()::isAnnotationPresent);
    }

    /**
     * tests if the field type is {@link Domain @Domain} in DOMA .
     *
     * @param field a member of the entity
     * @return true if the field type is {@link Domain @Domain} in DOMA
     */
    static boolean isDomainField(final Field field) {
      return Stream.of(Domain.class, DomainsAware.class).anyMatch(field.getDeclaringClass()::isAnnotationPresent);
    }

    /**
     * tests if the field type is the class which contains field (s) which related to a database column .
     *
     * @param field a member of the entity
     * @return true if the field is one of primary key
     */
    static boolean isEmbeddable(final Field field) {
      return fieldTypeIsAnnotated(field, Embeddable.class);
    }

    /**
     * tests if the field type is the class which contains field (s) which related to a database column .
     *
     * @param field a member of the entity
     * @return true if the field is one of primary key
     */
    static boolean isEmbeddableField(final Field field) {
      return field.getDeclaringClass().isAnnotationPresent(Embeddable.class);
    }

    /**
     * tests if the field is one of primary key .
     *
     * @param field a member of the entity
     * @return true if the field is one of primary key
     */
    static boolean isIdentity(final Field field) {
      return isAnnotated(field, Id.class);
    }

    /**
     * tests if the field does not related to a database column .
     *
     * @param field a member of the entity
     * @return true if the field does not related to a database column
     */
    static boolean isNotPersistive(final Field field) {
      return Streamr.Filter.anyOf(Streamr.stream(field), Field::isSynthetic, Reflections::isStatic, (t) -> isAnnotated(t, Transient.class)/* , (t) -> isEmbeddableField(t) && !isAnnotated(t, Column.class) */).count() > 0;
    }

    /**
     * tests if the field is related to a database column .
     *
     * @param field a member of the entity
     * @return true if the field is related to a database column
     */
    static boolean isPersistive(final Field field) {
      return Trebuchet.Predicates.orNot(field, Predicate.not(Predicates::isNotPersistive)::test);
    }
  }

  /** the type of an entity which has referred by this inspector . */
  private final Class<ENTITY> entityClass;

  /** the types a family of this {@link #entityClass} . */
  @Getter(value = AccessLevel.PROTECTED)
  private final List<Class<?>> classes;

  /** {@link NamingType} which specified this {@link #entityClass} . */
  @Getter
  private final NamingType namingType;

  /** the fields a member of this {@link #entityClass} which related to a database column . */
  @Getter
  private final List<Field> fields;

  private Inspector(Class<ENTITY> entityClass) {
    this.entityClass = Objects.requireNonNull(entityClass);
    classes = Collections.unmodifiableList(Entities.familyze(entityClass));
    namingType = Entities.Names.getNamingType(getClasses().toArray(Class<?>[]::new)).orElse(NamingType.NONE);
    fields = Entities.getColumnFields(getClasses().toArray(Class<?>[]::new), (field) -> Entities.Names.getName(field, Entities.Names.getNamingType(getClasses().toArray(Class<?>[]::new)).orElse(NamingType.NONE)));
  }

  /**
   * a static factory of {@link Inspector} .
   *
   * @param <ENTITY> the type of entity
   * @param entityClass the type of entity .
   * @return an inspector
   */
  public static <ENTITY extends Origin> Inspector<ENTITY> of(final Class<ENTITY> entityClass) {
    return new Inspector<>(entityClass);
  }

  /**
   * returns fields which related to a database column .
   *
   * @param condition a condition for exclusion, eg. {@link Field#getName()}
   * @param excludeConditions values for exclusion, they must be implemets {@link #equals(Object)}
   * @return fields which related to a database column
   */
  public final Field getField(String fieldName) {
    return Streamr.Filter.filtering(getFields(), (field) -> field.getName().equalsIgnoreCase(Objects.toString(fieldName, null))).findFirst().orElse(null);
  }

  /**
   * returns fields which related to a database column .
   *
   * @param condition a condition for exclusion, eg. {@link Field#getName()}
   * @param excludeConditions values for exclusion, they must be implemets {@link #equals(Object)}
   * @return fields which related to a database column
   */
  @SafeVarargs
  public final <T> List<Field> getFields(Function<Field, ? extends T> condition, T... excludeConditions) {
    return Entities.getColumnFields(getClasses().toArray(Class<?>[]::new), condition, excludeConditions);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @return the name which converted in the rule of database naming
   */
  public final String getName() {
    return Entities.Names.getName(entityClass);
  }

  /**
   * returns the name which converted in the rule of database naming .
   *
   * @param field a member of the entity
   * @return the name which converted in the rule of database naming
   */
  public final String getName(Field field) {
    return Entities.Names.getName(field, getNamingType());
  }
}
