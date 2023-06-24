package io.resys.thena.docdb.spi.objects;

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
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ImmutableBranchObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BranchObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BranchStateBuilder;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableObjectsResult;
import io.resys.thena.docdb.api.models.Objects.Branch;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class BranchStateBuilderDefault implements BranchStateBuilder {
  private final ClientState state;
  private final List<BlobCriteria> blobCriteria = new ArrayList<>();
  private String repo; //repo name
  private String ref;
  private boolean blobs;
  @Override public BranchStateBuilderDefault blobCriteria(List<BlobCriteria> blobCriteria) { this.blobCriteria.addAll(blobCriteria); return this; }

  @Override
  public BranchStateBuilder blobs() {
    this.blobs = true;
    return this;
  }
  @Override
  public Uni<ObjectsResult<BranchObjects>> build() {
    RepoAssert.notEmpty(repo, () -> "repoName is not defined!");
    RepoAssert.notEmpty(ref, () -> "ref is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var ex = RepoException.builder().notRepoWithName(repo);
        log.warn(ex.getText());
        return Uni.createFrom().item(ImmutableObjectsResult
            .<BranchObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(ex)
            .build());
      }
      return getRef(existing, ref, state.withRepo(existing));
    });
  }
  
  private Uni<ObjectsResult<BranchObjects>> getRef(Repo repo, String refName, ClientRepoState ctx) {

    return ctx.query().refs().name(refName).onItem()
        .transformToUni(ref -> {
          if(ref == null) {
            return ctx.query().refs().findAll().collect().asList().onItem().transform(allRefs -> 
              (ObjectsResult<BranchObjects>) ImmutableObjectsResult
              .<BranchObjects>builder()
              .repo(repo)
              .status(ObjectsStatus.OK)
              .addMessages(RepoException.builder().noRepoRef(
                  repo.getName(), refName, 
                  allRefs.stream().map(e -> e.getName()).collect(Collectors.toList())))
              .build()
            );
          }
          return getState(repo, ref, ctx);
        });
  }
  
  private Uni<ObjectsResult<BranchObjects>> getState(Repo repo, Branch ref, ClientRepoState ctx) {
    return ObjectsUtils.getCommit(ref.getCommit(), ctx).onItem()
        .transformToUni(commit -> ObjectsUtils.getTree(commit, ctx).onItem()
        .transformToUni(tree -> {
          if(this.blobs) {
            return ObjectsUtils.getBlobs(tree, blobCriteria, ctx)
              .onItem().transform(blobs -> ImmutableObjectsResult.<BranchObjects>builder()
                .repo(repo)
                .objects(ImmutableBranchObjects.builder()
                    .repo(repo)
                    .ref(ref)
                    .tree(tree)
                    .blobs(blobs)
                    .commit(commit)
                    .build())
                .repo(repo)
                .status(ObjectsStatus.OK)
                .build());
          }
          
          return Uni.createFrom().item(ImmutableObjectsResult.<BranchObjects>builder()
            .repo(repo)
            .objects(ImmutableBranchObjects.builder()
                .repo(repo)
                .ref(ref)
                .tree(tree)
                .commit(commit)
                .build())
            .status(ObjectsStatus.OK)
            .build());
        }));
  }
}
