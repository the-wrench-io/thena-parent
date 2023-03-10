package io.resys.thena.docdb.sql.queries;

import java.util.Optional;

import io.resys.thena.docdb.api.LogConstants;

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

import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.spi.ClientQuery.RefLock;
import io.resys.thena.docdb.spi.ClientQuery.RefLockStatus;
import io.resys.thena.docdb.spi.ClientQuery.RefQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ImmutableRefLock;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class RefQuerySqlPool implements RefQuery {

  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Ref> nameOrCommit(String refNameOrCommit) {
    RepoAssert.notEmpty(refNameOrCommit, () -> "refNameOrCommit must be defined!");
    final var sql = sqlBuilder.refs().getByNameOrCommit(refNameOrCommit);
    if(log.isDebugEnabled()) {
      log.debug("Ref refNameOrCommit query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Ref> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by refNameOrCommit: '" + refNameOrCommit + "'!", e));
  }
  @Override
  public Uni<Ref> get() {
    final var sql = sqlBuilder.refs().getFirst();
    if(log.isDebugEnabled()) {
      log.debug("Ref get query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }

    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute()
      .onItem()
      .transform((RowSet<Ref> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF'!", e));
  }
  @Override
  public Multi<Ref> findAll() {
    final var sql = sqlBuilder.refs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Ref findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute()
      .onItem()
      .transformToMulti((RowSet<Ref> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF'!", e));
  }
  @Override
  public Uni<Ref> name(String name) {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    final var sql = sqlBuilder.refs().getByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Ref getByName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Ref> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by name: '" + name + "'!", e));
  }
  @Override
  public Uni<RefLock> lockName(String name) {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    final var sql = sqlBuilder.refs().getLockByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Ref lockName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Ref> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return (RefLock) ImmutableRefLock.builder()
              .ref(Optional.of(it.next()))
              .status(RefLockStatus.LOCK_TAKEN)
              .message(Optional.empty())
              .build();
        }
        return (RefLock) ImmutableRefLock.builder()
            .ref(Optional.empty())
            .message(Optional.empty())
            .status(RefLockStatus.NOT_FOUND)
            .build();
      })
      /*
      .onFailure(throwable -> errorHandler.isLocked(throwable)).recoverWithItem((error) -> (RefLock) 
          ImmutableRefLock.builder()
            .ref(Optional.empty())
            .status(RefLockStatus.BLOCKED)
            .message(Optional.ofNullable(error.getMessage()))
            .build()
      )*/
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by name: '" + name + "'!", e));
  }
}
