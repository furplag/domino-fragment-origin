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

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;
import org.seasar.doma.jdbc.builder.SelectBuilder;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.furplag.sandbox.domino.misc.TestConfig;
import jp.furplag.sandbox.domino.misc.vars.ColumnDef;

class RowOriginTest implements RowOrigin {

  public static class Zero implements RowOrigin {
    @Table(name = "oneTableNameSpacified")
    public static class One extends Zero {}

    @Table
    public static class Two extends Zero.One {}

    @Entity(naming = NamingType.UPPER_CASE)
    public static class Three extends Zero.One {}

    @Entity(naming = NamingType.LOWER_CASE)
    public static class Four extends Zero.Three {}

    @Table(name = "FI_ve")
    @Entity(naming = NamingType.LOWER_CASE)
    public static class Five extends Zero.Three {}
  }

  public static class One implements RowOrigin {


    @Id
    private long theIdentity;

    @Column(name = "a_column")
    private boolean aColumn;

    @SuppressWarnings({ "unused" })
    private String andAlsoColumn;

    @Transient
    private int notACloumn;

    @Entity(naming = NamingType.UPPER_CASE)
    public static class Two extends One {}

    @Entity(naming = NamingType.SNAKE_LOWER_CASE)
    public static class Three extends One {}

    @Entity(naming = NamingType.LOWER_CASE)
    public static class Four extends One.Three {
      @SuppressWarnings({ "unused" })
      private final int extend = 1;
    }

    @Entity(naming = NamingType.LOWER_CASE)
    public static class Five extends One.Four {

    }

    @Entity(naming = NamingType.LOWER_CASE)
    public static class Six extends One.Four {}
  }

  @Test
  void test() {
    // @formatter:off
    assertAll(
        () -> assertEquals("select * from Zero", new Zero().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select * from oneTableNameSpacified", new Zero.One().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select * from Two", new Zero.Two().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select * from THREE", new Zero.Three().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select * from four", new Zero.Four().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
      , () -> assertEquals("select * from FI_ve", new Zero.Five().select(SelectBuilder.newInstance(TestConfig.singleton())).getSql().toString())
    );

    assertAll(
        () -> assertEquals("theIdentity, a_column, andAlsoColumn", new One().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("THEIDENTITY, a_column, ANDALSOCOLUMN", new One.Two().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("the_identity, a_column, and_also_column", new One.Three().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
      , () -> assertEquals("theidentity, a_column, andalsocolumn, extend", new One.Four().getColumns().stream().map(ColumnDef::getColumnName).collect(Collectors.joining(", ")))
    );
    // @formatter:on
  }
}
