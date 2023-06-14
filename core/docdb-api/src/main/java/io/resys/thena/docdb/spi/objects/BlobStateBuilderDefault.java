package io.resys.thena.docdb.spi.objects;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.ImmutableBlobObject;
import io.resys.thena.docdb.api.actions.ImmutableBlobObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobStateBuilder;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableObjectsResult;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
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
@Data @Accessors(fluent = true)
@RequiredArgsConstructor
public class BlobStateBuilderDefault implements BlobStateBuilder {
  private final ClientState state;
  private final List<BlobCriteria> blobCriteria = new ArrayList<>();
  private String repo;
  private String anyId; //anyId;
  private List<String> blobName = new ArrayList<>();

  @Override
  public BlobStateBuilder ref(String refId) {
    this.anyId = refId;
    return this;
  }
  @Value.Immutable
  public static interface BlobAndTree {
    List<Blob> getBlob();
    String getTreeId();
  }
  @Override
  public BlobStateBuilderDefault blobName(String blobName) {
    this.blobName.add(blobName);
    return this;
  }
  @Override
  public BlobStateBuilderDefault blobNames(List<String> blobName) {
    this.blobName.addAll(blobName);
    return this;
  }
  @Override
  public BlobStateBuilder blobCriteria(List<BlobCriteria> blobCriteria) {
    this.blobCriteria.addAll(blobCriteria);
    return this;
  }
  
  @Override
  public Uni<ObjectsResult<BlobObjects>> list() {
    RepoAssert.notEmpty(repo, () -> "repoName is not defined!");
    RepoAssert.notEmpty(anyId, () -> "anyId is not defined!");
    RepoAssert.isTrue(!blobName.isEmpty(), () -> "blobName is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        return objectsError(RepoException.builder().notRepoWithName(repo));
      }
      final var ctx = state.withRepo(existing);
      return ObjectsUtils.findCommit(ctx, anyId).onItem().transformToUni(commit -> {
        if(commit == null) {
          final var error = RepoException.builder().noCommit(existing, anyId);
          return objectsError(error); 
        }
        return getListState(existing, commit, ctx);
      });
    });
  }
  
  @Override
  public Uni<ObjectsResult<BlobObject>> get() {
    RepoAssert.notEmpty(repo, () -> "repoName is not defined!");
    RepoAssert.notEmpty(anyId, () -> "anyId is not defined!");
    RepoAssert.isTrue(!blobName.isEmpty(), () -> "blobName is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var error = RepoException.builder().notRepoWithName(repo);
        return objectsError(error);
      }
      final var ctx = state.withRepo(existing);
      return ObjectsUtils.findCommit(ctx, anyId).onItem().transformToUni(commit -> {
          if(commit == null) {
            final var error = RepoException.builder().noCommit(existing, anyId);
            return objectsError(error); 
          }
          return getState(existing, commit, ctx);
        });
    });
  }

  private <T> Uni<ObjectsResult<T>> objectsError(Message error) {
    log.error(error.getText());
    return Uni.createFrom().item(ImmutableObjectsResult
        .<T>builder()
        .status(ObjectsStatus.ERROR)
        .addMessages(error)
        .build());
  }

  
  private Uni<ObjectsResult<BlobObject>> getState(Repo repo, Commit commit, ClientRepoState ctx) {
    return getBlob(commit.getTree(), ctx, blobCriteria, blobName).onItem()
        .transformToUni(blobTree -> {
          
          if(blobTree.getBlob().size() != 1) {
            final var error = RepoException.builder()
                .noBlob(repo, commit.getTree(), commit.getId(), blobName.toArray(new String[] {}));
            return objectsError(error); 
          }
          
          return Uni.createFrom().item(ImmutableObjectsResult.<BlobObject>builder()
            .repo(repo)
            .objects(ImmutableBlobObject.builder()
                .repo(repo)
                .commit(commit)
                .blob(blobTree.getBlob().isEmpty() ? null : blobTree.getBlob().get(0))
                .build())
            .status(ObjectsStatus.OK)
            .build());
        });
  
  }
  
  private Uni<ObjectsResult<BlobObjects>> getListState(Repo repo, Commit commit, ClientRepoState ctx) {
    return getBlob(commit.getTree(), ctx, blobCriteria, blobName).onItem()
        .transformToUni(blobAndTree -> {
          
          if(blobAndTree.getBlob().isEmpty()) {
            final var error = RepoException.builder()
                .noBlob(repo, blobAndTree.getTreeId(), commit.getId(), blobName.toArray(new String[] {}));
            return objectsError(error); 
          }
          return Uni.createFrom().item(ImmutableObjectsResult.<BlobObjects>builder()
            .repo(repo)
            .objects(ImmutableBlobObjects.builder()
                .repo(repo)
                .commit(commit)
                .blob(blobAndTree.getBlob())
                .build())
            .status(ObjectsStatus.OK)
            .build());
        });
  
  }

  private static Uni<BlobAndTree> getBlob(String treeId, ClientRepoState ctx, List<BlobCriteria> blobCriteria, List<String> blobName) {
    if(!blobName.isEmpty()) {
      return ctx.query().blobs().findAll(treeId, blobName, blobCriteria).collect().asList()
          .onItem().transform(blobs -> ImmutableBlobAndTree.builder().blob(blobs).treeId(treeId).build());
    }
    
    return Uni.createFrom().item(ImmutableBlobAndTree.builder().treeId(treeId).build());
  }
}
