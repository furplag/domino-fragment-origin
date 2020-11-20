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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import jp.furplag.sandbox.trebuchet.Trebuchet;
import lombok.Getter;
import lombok.NonNull;

public interface Where<T> extends Comparable<Where<?>> {

  /**
   * conditional operators of where clause in SQL query .
   *
   * @author furplag
   *
   */
  @Getter
  public static enum Operator {
    // @formatter:off
    Null("is", true), NotNull("is", true, true, false),
    Equal("="), NotEqual("=", false, true, false),
    Contains("like", false, false, true), EndsWith("like", false, false, true), StartsWith("like", false, false, true),
    Except("like", false, true, true), NotEndsWith("like", false, true, true), NotStartsWith("like", false, true, true),
    GreaterThan(">"), GreaterThanEqual(">="),
    LessThan("<"), LessThanEqual("<="),
    Includes("in"), Excludes("in", false, true, false);
    // @formatter:on

    /** conditional operator of where clause . */
    private final String operator;

    /** conditional operator of where clause to filtering null values . */
    private final boolean nullFinder;

    /** conditional operator of where clause to negation . */
    private final boolean negate;

    /** conditional operator of where clause to search keyword . */
    private final boolean searcher;

    /**
     *
     * @param operator conditional operator of where clause
     */
    private Operator(String operator) {
      this(operator, false, false, false);
    }

    /**
     *
     * @param operator conditional operator of where clause
     */
    private Operator(String operator, boolean nullFinder) {
      this(operator, nullFinder, false, false);
    }

    /**
     *
     * @param operator conditional operator of where clause
     */
    private Operator(String operator, boolean nullFinder, boolean negate, boolean searcher) {
      this.operator = StringUtils.defaultIfBlank(operator, "=");
      this.nullFinder = nullFinder;
      this.negate = negate;
      this.searcher = searcher;
    }
  }

  static final class AnyOf<T> extends Origin<T> {

    private AnyOf(Var.AnyOf<T> var, Operator operator) {
      super(var, operator);
      if (!List.of(Operator.Includes, Operator.Excludes).contains(operator)) {
        throw new IllegalArgumentException(String.format("the operator \"%s\" could not use \"in\" .", operator.name()));
      }
    }

    @Override
    public @NonNull Var.AnyOf<T> getVar() {
      return (Var.AnyOf<T>) super.getVar();
    }

    @Override
    public SelectBuilder sql(SelectBuilder selectBuilder) {
      return selectBuilder.sql(String.join(" ", getOperator().isNegate() ? " not" : "", getVar().getColumnName(), getOperator().getOperator(), "(")).params(getVar().getValueType(), getVar().getValues()).sql(")");
    }
  }

  static abstract class Origin<T> implements Where<T> {

    /** field deffinition represented by {@link Var} . */
    @Getter
    final Var<T> var;

    /** conditional operator of where clause . */
    @Getter
    final Operator operator;

    private Origin(@NonNull Var<T> var, @NonNull Operator operator) {
      this.var = var;
      this.operator = Objects.requireNonNullElse(operator, Objects.isNull(var.getValue()) && operator.isNegate() ? Operator.NotNull : Operator.Null);
    }

  }

  static final class Word<T> extends Origin<T> {

    private Word(Var<T> var, @NonNull Operator operator) {
      super(var, Objects.toString(var.getValue(), "").isEmpty() && operator.isNegate() ? Operator.NotEqual : Objects.toString(var.getValue(), "").isEmpty() ? Operator.Equal : operator);
      if (!operator.isSearcher()) {
        throw new IllegalArgumentException(String.format("the operator \"%s\" could not use \"like\" .", operator.name()));
      }
    }

    private String getValue() {
      final String prefix = Stream.of(Operator.Contains, Operator.EndsWith, Operator.Except, Operator.NotEndsWith).anyMatch(getOperator()::equals) ? "%" : "";
      final String suffix = Stream.of(Operator.Contains, Operator.Except, Operator.NotStartsWith, Operator.StartsWith).anyMatch(getOperator()::equals) ? "%" : "";

      return String.join("", prefix, Objects.toString(getVar().getValue(), ""), suffix);
    }

    @Override
    public SelectBuilder sql(SelectBuilder selectBuilder) {
      if (Stream.of(Operator.Equal, Operator.NotEqual).anyMatch(getOperator()::equals)) {
        return super.sql(selectBuilder);
      }

      return selectBuilder.sql(String.join(" ", getOperator().isNegate() ? " not" : "", getVar().getColumnName(), getOperator().getOperator(), " ")).param(String.class, getValue()).sql(" ");
    }
  }

  static final class Range<T extends Comparable<T>> extends Origin<T> {

    private Range(Var.Range<T> var, boolean containsEqual) {
      super(var, containsEqual ? Operator.LessThanEqual : Operator.LessThan);
      if (var.getValueCount() < 1) {
        throw new IllegalArgumentException(String.format("at least one value required ."));
      }
    }

    @Override
    public Var.Range<T> getVar() {
      return (Var.Range<T>) super.getVar();
    }

    @Override
    public SelectBuilder sql(SelectBuilder selectBuilder) {
      selectBuilder.sql(String.join(" ", getVar().getValueCount() > 1 ? " (" : " ", getVar().getColumnName(), (Objects.nonNull(getVar().getMin()) ? getOperator() : (Operator.LessThanEqual.equals(getOperator()) ? Operator.GreaterThanEqual : Operator.GreaterThan)).getOperator()))
        .param(getVar().getValueType(), Objects.requireNonNullElse(getVar().getMin(), getVar().getMax()));
      if (getVar().getValueCount() > 1) {
        selectBuilder.sql(String.join(" ", "and", getVar().getColumnName(), (Operator.LessThanEqual.equals(getOperator()) ? Operator.GreaterThanEqual : Operator.GreaterThan).getOperator())).param(getVar().getValueType(), getVar().getMax()).sql(") ");
      }

      return selectBuilder;
    }
  }

  static <T extends Comparable<T>> Where<T> rangeOf(final Var.Range<T> var, final boolean containsEqual) {
    return new Range<>(var, containsEqual);
  }

  static <T> Where<T> of(final Var<T> var, final @NonNull Operator operator) {
    return operator.isSearcher() ? new Word<>(var, operator) : var instanceof Var.AnyOf ? new AnyOf<>((Var.AnyOf<T>) var, operator) : new Where.Origin<>(var, operator) {};
  }

  /**
   * returns conditional operator of {@link Where} .
   *
   * @return conditional operator of {@link Where} .
   */
  Operator getOperator();

  /**
   * returns the value of condition .
   *
   * @return the value of condition .
   */
  Var<T> getVar();

  default SelectBuilder sql(SelectBuilder selectBuilder) {
    selectBuilder.sql(String.join(" ", getOperator().isNegate() ? " not" : "", getVar().getColumnName(), getOperator().getOperator(), getOperator().isNullFinder() ? "NULL" : ""));

    return getOperator().isNullFinder() ? selectBuilder : selectBuilder.param(getVar().getValueType(), getVar().getValue());
  }

  /** {@inheritDoc} */
  @Override
  default int compareTo(Where<?> anotherOne) {
    return getVar().compareTo(Trebuchet.Functions.orNot(anotherOne, Where::getVar));
  }
}
