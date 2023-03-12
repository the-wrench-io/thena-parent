package io.resys.thena.docdb.spi.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.actions.CommitActions.CommitObjects;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStateBuilder;
import io.resys.thena.docdb.api.actions.ImmutableCommitObjects;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableObjectsResult;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.objects.ObjectsUtils;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CommitStateBuilderImpl implements CommitStateBuilder {
  private final ClientState state;
  private final List<BlobCriteria> blobCriteria = new ArrayList<>();
  private String repo;
  private String anyId; //refOrCommitOrTag
  private boolean blobs;
  
  @Override public CommitStateBuilderImpl blobCriteria(List<BlobCriteria> blobCriteria) { this.blobCriteria.addAll(blobCriteria); return this; }
  @Override public CommitStateBuilder blobs() { this.blobs = true; return this; }
  
  @Override
  public Uni<ObjectsResult<CommitObjects>> build() {
    RepoAssert.notEmpty(repo, () -> "repo is not defined!");
    RepoAssert.notEmpty(anyId, () -> "refOrCommitOrTag is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var error = RepoException.builder().notRepoWithName(repo);
        log.error(error.getText());
        return Uni.createFrom().item(ImmutableObjectsResult
            .<CommitObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(error)
            .build());
      }
      final var ctx = state.withRepo(existing);
      
      return ObjectsUtils.findCommit(ctx, anyId)
        .onItem().transformToUni(commit -> {
          if(commit == null) {
            final var error = RepoException.builder().noCommit(existing, anyId);
            log.error(error.getText());
            return Uni.createFrom().item(ImmutableObjectsResult
                .<CommitObjects>builder()
                .status(ObjectsStatus.ERROR)
                .addMessages(error)
                .build()); 
          }
          return getState(existing, commit, ctx);
        });
    });
  }
  
  
  private Uni<ObjectsResult<CommitObjects>> getState(Repo repo, Commit commit, ClientRepoState ctx) {
    return ObjectsUtils.getTree(commit, ctx).onItem()
    .transformToUni(tree -> {
      if(this.blobs) {
        return getBlobs(tree, ctx, blobCriteria)
          .onItem().transform(blobs -> ImmutableObjectsResult.<CommitObjects>builder()
            .repo(repo)
            .objects(blobs
                .repo(repo)
                .tree(tree)
                .commit(commit)
                .build())
            .repo(repo)
            .status(ObjectsStatus.OK)
            .build());
      }
      
      return Uni.createFrom().item(ImmutableObjectsResult.<CommitObjects>builder()
        .repo(repo)
        .objects(ImmutableCommitObjects.builder()
            .repo(repo)
            .tree(tree)
            .commit(commit)
            .build())
        .status(ObjectsStatus.OK)
        .build());
    });
  }
  
  private static Uni<ImmutableCommitObjects.Builder> getBlobs(Tree tree, ClientRepoState ctx, List<BlobCriteria> blobCriteria) {
    return ctx.query().blobs().findAll(tree.getId(), blobCriteria)
        .collect().asList().onItem()
        .transform(blobs -> {
          final var objects = ImmutableCommitObjects.builder();
          blobs.forEach(blob -> objects.putBlobs(blob.getId(), blob));
          return objects;
        });
  }
}
