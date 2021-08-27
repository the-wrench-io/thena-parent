package io.resys.thena.docdb.spi.pgsql.builders;

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

import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientQuery.CommitQuery;
import io.resys.thena.docdb.spi.pgsql.sql.PgErrors;
import io.resys.thena.docdb.spi.sql.SqlBuilder;
import io.resys.thena.docdb.spi.sql.SqlMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;

public class PgCommitQuery implements CommitQuery {

  private final PgPool client;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;

  public PgCommitQuery(PgPool client, ClientCollections names, SqlMapper sqlMapper, SqlBuilder sqlBuilder) {
    super();
    this.client = client;
    this.sqlMapper = sqlMapper;
    this.sqlBuilder = sqlBuilder;
  }
  @Override
  public Uni<Commit> id(String commit) {
    final var sql = sqlBuilder.commits().getById(commit);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.commit(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Commit> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> PgErrors.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't find 'COMMIT' by 'id': '" + commit + "'!", e));
  }
  @Override
  public Multi<Commit> find() {
    final var sql = sqlBuilder.commits().findAll();
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.commit(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Commit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't find 'COMMIT'!", e));
  }
}
