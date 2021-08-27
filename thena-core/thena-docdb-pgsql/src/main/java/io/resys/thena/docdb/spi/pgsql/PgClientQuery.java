package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-mongo
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

import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.pgsql.builders.PgBlobQuery;
import io.resys.thena.docdb.spi.pgsql.builders.PgCommitQuery;
import io.resys.thena.docdb.spi.pgsql.builders.PgRefQuery;
import io.resys.thena.docdb.spi.pgsql.builders.PgTagQuery;
import io.resys.thena.docdb.spi.pgsql.builders.PgTreeQuery;
import io.resys.thena.docdb.spi.pgsql.support.ClientWrapper;
import io.resys.thena.docdb.spi.sql.SqlBuilder;
import io.resys.thena.docdb.spi.sql.SqlMapper;

public class PgClientQuery implements ClientQuery {
  
  private final ClientWrapper wrapper;
  private final SqlBuilder sqlBuilder;
  private final SqlMapper sqlMapper;
  
  public PgClientQuery(ClientWrapper wrapper, SqlMapper sqlMapper, SqlBuilder sqlBuilder) {
    this.wrapper = wrapper;
    this.sqlBuilder = sqlBuilder;
    this.sqlMapper = sqlMapper;
  }

  @Override
  public TagQuery tags() {
    return new PgTagQuery(wrapper.getClient(), wrapper.getNames(), sqlMapper, sqlBuilder);
  }

  @Override
  public CommitQuery commits() {
    return new PgCommitQuery(wrapper.getClient(), wrapper.getNames(), sqlMapper, sqlBuilder);
  }

  @Override
  public RefQuery refs() {
    return new PgRefQuery(wrapper.getClient(), wrapper.getNames(), sqlMapper, sqlBuilder);
  }

  @Override
  public TreeQuery trees() {
    return new PgTreeQuery(wrapper.getClient(), wrapper.getNames(), sqlMapper, sqlBuilder);
  }

  @Override
  public BlobQuery blobs() {
    return new PgBlobQuery(wrapper.getClient(), wrapper.getNames(), sqlMapper, sqlBuilder);
  }
}
