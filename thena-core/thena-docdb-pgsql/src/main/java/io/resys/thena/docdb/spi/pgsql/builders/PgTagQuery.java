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

import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientQuery.DeleteResult;
import io.resys.thena.docdb.spi.ClientQuery.TagQuery;
import io.resys.thena.docdb.spi.ImmutableDeleteResult;
import io.resys.thena.docdb.spi.pgsql.sql.PgErrors;
import io.resys.thena.docdb.spi.sql.SqlBuilder;
import io.resys.thena.docdb.spi.sql.SqlMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;

public class PgTagQuery implements TagQuery {
  
  private final PgPool client;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private String name;

  public PgTagQuery(PgPool client, ClientCollections names, SqlMapper sqlMapper, SqlBuilder sqlBuilder) {
    super();
    this.client = client;
    this.sqlMapper = sqlMapper;
    this.sqlBuilder = sqlBuilder;
  }
  @Override
  public TagQuery name(String name) {
    this.name = name;
    return this;
  }
  @Override
  public Uni<DeleteResult> delete() {
    final var sql = sqlBuilder.tags().deleteByName(name);
    return client.preparedQuery(sql.getValue())
        .execute(sql.getProps())
        .onItem()
        .transform(result -> (DeleteResult) ImmutableDeleteResult.builder().deletedCount(1).build())
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't delete 'TAG' by name: '" + name + "'!", e));
  }
  @Override
  public Uni<Tag> get() {
    final var sql = sqlBuilder.tags().getFirst();
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute()
        .onItem()
        .transform((RowSet<Tag> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't find 'TAG'!", e));      
  }
  @Override
  public Multi<Tag> find() {
    if(name == null || name.isBlank()) {
      final var sql = sqlBuilder.tags().findAll();
      return client.preparedQuery(sql.getValue())
          .mapping(row -> sqlMapper.tag(row))
          .execute()
          .onItem()
          .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
          .onFailure().invoke(e -> PgErrors.deadEnd("Can't find 'TAG'!", e));      
    }
    final var sql = sqlBuilder.tags().getByName(name);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't find 'TAG' by name: '" + name + "'!", e));   
  }
}
