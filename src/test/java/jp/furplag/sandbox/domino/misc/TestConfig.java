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

import org.seasar.doma.SingletonConfig;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import lombok.Getter;

@SingletonConfig
public class TestConfig implements org.seasar.doma.jdbc.Config {

  private static final TestConfig INSTANCE = new TestConfig();

  @Getter
  private final Dialect dialect;

  @Getter
  private final LocalTransactionDataSource dataSource;

  @Getter
  private final TransactionManager transactionManager;

  private TestConfig() {
    dialect = new H2Dialect();
    dataSource = new LocalTransactionDataSource("jdbc:h2:./.data/domaf;mode=PostgreSQL", "domaf", "domaf");
    transactionManager =  new LocalTransactionManager(dataSource.getLocalTransaction(getJdbcLogger()));
  }

  public static TestConfig singleton() {
    return INSTANCE;
  }
}
