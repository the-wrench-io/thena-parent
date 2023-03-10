package io.resys.thena.docdb.file.builders;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ImmutableCommitLock;

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
import io.resys.thena.docdb.api.models.Objects.CommitLock;
import io.resys.thena.docdb.api.models.Objects.CommitLockStatus;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.CommitQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

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
  public Uni<CommitLock> getLock(String commitId, String headName) {
    
    return new RefQueryFilePool(client, mapper, builder, errorHandler).nameOrCommit(headName).onItem().transformToUni(ref -> {
      if(ref == null) {
        return Uni.createFrom().item((CommitLock) ImmutableCommitLock.builder()
          .ref(Optional.empty())
          .commit(Optional.empty())
          .tree(Optional.empty())
          .message(Optional.empty())
          .status(CommitLockStatus.NOT_FOUND)
          .build());
      }
      
      return getById(ref.getCommit()).onItem().transformToUni(commit -> {
        final var treeUni = new TreeQueryFilePool(client, mapper, builder, errorHandler).getById(commit.getTree());
        final var blobUni = new BlobQueryFilePool(client, mapper, builder, errorHandler).findByTreeId(commit.getTree()).collect().asList();        
        
        return Uni.combine().all().unis(treeUni, blobUni).asTuple().onItem().transform(tuple -> {
          return (CommitLock) ImmutableCommitLock.builder()
          .ref(ref)
          .commit(commit)
          .tree(tuple.getItem1())
          .message(Optional.empty())
          .status(CommitLockStatus.LOCK_TAKEN)
          .blobs(tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build();
        });
      });
    });
  }
}
