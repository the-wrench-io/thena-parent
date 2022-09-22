package io.resys.thena.docdb.sql.builders;

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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ClientState.RepoBuilder;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClientHelper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PgRepoBuilder implements RepoBuilder {
  private final io.vertx.mutiny.sqlclient.Pool client;
  private final ClientCollections names;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;


  @Override
  public Uni<Repo> getByName(String name) {
    final var sql = sqlBuilder.repo().getByName(name);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.repo(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          errorHandler.deadEnd("Can't find 'REPOS' by 'name'!", e);
        });
  }

  @Override
  public Uni<Repo> getByNameOrId(String nameOrId) {
    final var sql = sqlBuilder.repo().getByNameOrId(nameOrId);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.repo(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REPOS' by 'name' or 'id'!", e));
  }
  
  @Override
  public Uni<Repo> insert(final Repo newRepo) {
    final var next = names.toRepo(newRepo);
    final var sqlBuilder = this.sqlBuilder.withOptions(next);
    
    return SqlClientHelper.inTransactionUni(client, tx -> {
      final var repoInsert = sqlBuilder.repo().insertOne(newRepo);
      final var tablesCreate = new StringBuilder()
          .append(sqlBuilder.blobs().create().getValue())
          .append(sqlBuilder.commits().create().getValue())
          .append(sqlBuilder.treeItems().create().getValue())
          .append(sqlBuilder.trees().create().getValue())
          .append(sqlBuilder.refs().create().getValue())
          .append(sqlBuilder.tags().create().getValue())
          
          .append(sqlBuilder.commits().constraints().getValue())
          .append(sqlBuilder.refs().constraints().getValue())
          .append(sqlBuilder.tags().constraints().getValue())
          .append(sqlBuilder.treeItems().constraints().getValue())
          .toString();      
      final Uni<Void> create = client.preparedQuery(sqlBuilder.repo().create().getValue()).execute()
          .onItem().transformToUni(data -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't create table 'REPOS'!", e));;
      
      
      final Uni<Void> insert = tx.preparedQuery(repoInsert.getValue()).execute(repoInsert.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'REPO': '" + repoInsert.getValue() + "'!", e));
      final Uni<Void> nested = tx.query(tablesCreate).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't create tables: " + tablesCreate, e));;
      
      
      return create
          .onItem().transformToUni((junk) -> insert)
          .onItem().transformToUni((junk) -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }

  @Override
  public Multi<Repo> find() {
    return client.preparedQuery(this.sqlBuilder.repo().findAll().getValue())
    .mapping(row -> sqlMapper.repo(row))
    .execute()
    .onItem()
    .transformToMulti((RowSet<Repo> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure(e -> errorHandler.notFound(e)).recoverWithCompletion()
    .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REPOS'!", e));
  }

  @Override
  public Uni<Void> create() {
    return client.preparedQuery(this.sqlBuilder.repo().create().getValue()).execute()
    .onItem().transformToUni(data -> Uni.createFrom().voidItem())
    .onFailure().invoke(e -> errorHandler.deadEnd("Can't create table 'REPOS'!", e));
  }
}
