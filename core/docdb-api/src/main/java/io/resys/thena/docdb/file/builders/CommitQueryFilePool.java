package io.resys.thena.docdb.file.builders;

import java.util.Collection;
import java.util.Optional;

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
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.CommitLock;
import io.resys.thena.docdb.spi.ClientQuery.CommitLockStatus;
import io.resys.thena.docdb.spi.ClientQuery.CommitQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ImmutableCommitLock;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CommitQueryFilePool implements CommitQuery {
  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Commit> getById(String commit) {
    final var sql = builder.commits().getById(commit);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.commit(row))
        .execute()
        .onItem()
        .transform((Collection<Commit> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'COMMIT' by 'id': '" + commit + "'!", e));
  }
  @Override
  public Multi<Commit> findAll() {
    final var sql = builder.commits().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.commit(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Commit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'COMMIT'!", e));
  }
  @Override
  public Uni<CommitLock> getLock(String commitId) {
    log.warn("File pool is for local dev, no locking for commits");
    return getById(commitId).onItem().transform(e -> ImmutableCommitLock.builder()
        .commit(Optional.ofNullable(e)).message(Optional.empty())
        .status(e == null ? CommitLockStatus.NOT_FOUND : CommitLockStatus.LOCK_TAKEN)
        .build());
  }
}
