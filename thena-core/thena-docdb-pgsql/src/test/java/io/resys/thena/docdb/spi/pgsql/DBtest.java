package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.spi.pgsql.config.DbTestTemplate;
import io.resys.thena.docdb.spi.pgsql.config.PgProfile;

//-Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
@TestProfile(PgProfile.class)
public class DBtest extends DbTestTemplate {

//  
//  @RegisterExtension
//  final static QuarkusUnitTest config = new QuarkusUnitTest()
//    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
//      .addAsResource(new StringAsset(
//          "quarkus.datasource.db-kind=pg\r\n" +
//          ""), "application.properties")
//    );

  @Test
  public void test1() {

      System.out.println(LocalDate.now().toString().length());
  }
}
