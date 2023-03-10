package io.resys.thena.docdb.spi.commits;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.CommitActions.HeadCommitBuilder;
import io.resys.thena.docdb.api.actions.ImmutableCommitResult;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Objects.CommitLock;
import io.resys.thena.docdb.api.models.Objects.CommitLockStatus;
import io.resys.thena.docdb.spi.ClientInsertBuilder.Batch;
import io.resys.thena.docdb.spi.ClientInsertBuilder.BatchStatus;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.commits.CommitBatchBuilderImpl.CommitTreeState;
import io.resys.thena.docdb.spi.support.Identifiers;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class HeadCommitBuilderImpl implements HeadCommitBuilder {

  private final ClientState state;
  private final Map<String, JsonObject> appendBlobs = new HashMap<>();
  private final List<String> deleteBlobs = new ArrayList<>();
  
  private String headGid;
  private String repoId;
  private String headName;
  private String author;
  private String message;
  private String parentCommit;
  private Boolean parentIsLatest = Boolean.FALSE;

  @Override
  public HeadCommitBuilder id(String headGid) {
    RepoAssert.isEmpty(headName, () -> "Can't defined id when head is defined!");
    this.headGid = headGid;
    return this;
  }
  @Override
  public HeadCommitBuilder head(String repoId, String headName) {
    RepoAssert.isEmpty(headGid, () -> "Can't defined head when id is defined!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(headName, () -> "headName can't be empty!");
    RepoAssert.isName(headName, () -> "headName has invalid charecters!");
    this.repoId = repoId;
    this.headName = headName;
    return this;
  }
  @Override
  public HeadCommitBuilder append(String name, JsonObject blob) {
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' is already defined!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' can't be appended because it's been marked for removal!");
    this.appendBlobs.put(name, blob);
    return this;
  }
  @Override
  public HeadCommitBuilder remove(String name) {
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' can't be marked for removal because it's beed appended!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' is already marked for removal!");
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    this.deleteBlobs.add(name);
    return this;
  }
  @Override
  public HeadCommitBuilder author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public HeadCommitBuilder message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public HeadCommitBuilder parent(String parentCommit) {
    this.parentCommit = parentCommit;
    return this;
  }

  @Override
  public HeadCommitBuilder parentIsLatest() {
    this.parentIsLatest = true;
    return this;
  }
  @Override
  public Uni<CommitResult> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(!appendBlobs.isEmpty() || !deleteBlobs.isEmpty(), () -> "Nothing to commit, no content!");
    
    if(this.headGid != null) {
      final var id = Identifiers.fromRepoHeadGid(this.headGid);
      this.repoId = id[0];
      this.headName = id[1];
    }
    RepoAssert.notEmpty(repoId, () -> "Can't resolve repoId!");
    RepoAssert.notEmpty(headName, () -> "Can't resolve headName!");

    final var gid = Identifiers.toRepoHeadGid(repoId, headName);  
    return this.state.withTransaction(repoId, headName, tx -> {
      
      return tx.query().commits().getLock(parentCommit, headName)
        .onItem().transformToUni(lock -> {

          final var validation = validateRepo(lock, parentCommit);
          if(validation != null) {
            return Uni.createFrom().item(validation);
          }
          
          final var batch = doInLock(lock, tx);
          return tx.insert().batch(batch)
              .onItem().transform(rsp -> ImmutableCommitResult.builder()
                .gid(gid)
                .commit(rsp.getCommit())
                .addMessages(rsp.getLog())
                .addAllMessages(rsp.getMessages())
                .status(visitStatus(rsp.getStatus()))
                .build());
        });
    
    });
    
  }
  
  private Batch doInLock(CommitLock lock, ClientRepoState tx) {
    final var gid = Identifiers.toRepoHeadGid(repoId, headName);  
    final CommitTreeState init;
    if(lock.getStatus() == CommitLockStatus.NOT_FOUND) {
      // first commit
      init = CommitTreeState.builder().ref(lock.getRef()).refName(headName).gid(gid).repo(tx.getRepo()).build();
    } else {
      init = CommitTreeState.builder()
        .ref(lock.getRef())
        .refName(headName)
        .gid(gid)
        .repo(tx.getRepo())
        .commit(lock.getCommit())
        .tree(lock.getTree())
        .build();
    }
    
    return new CommitBatchBuilderImpl(init)
        .commitParent(parentCommit)
        .commitAuthor(author)
        .commitMessage(message)
        .toBeInserted(appendBlobs)
        .toBeRemoved(deleteBlobs)
        .build();
  }
  
  
  /**
   * 
   *     return CommitProcessorImpl.from(state).process(repoId, headName, parentCommit)
        .onItem().transformToUni(process -> process
            .commitAuthor(author)
            .commitMessage(message)
            .toBeInserted(appendBlobs)
            .toBeRemoved(deleteBlobs)
            .execute());

return (CommitBatchBuilder) new CommitBatchBuilderImpl(repoCtx, init);
    
      tx.insert().batch(batch);
.onItem().transform(rsp -> ImmutableCommitResult.builder()
          .gid(this.commitTree.getGid())
          .commit(rsp.getCommit())
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(visitStatus(rsp.getStatus()))
          .build())
   */
  

  private CommitResult validateRepo(CommitLock state, String commitParent) {
    final var gid = Identifiers.toRepoHeadGid(repoId, headName);
    // Unknown parent
    if(state.getCommit().isEmpty() && commitParent != null) {
      return (CommitResult) ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(gid).append("'")
                  .append(" is rejected.")
                  .append(" Your head is: '").append(commitParent).append("')")
                  .append(" but remote has no head.").append("'!")
                  .toString())
              .build())
          .status(CommitStatus.ERROR)
          .build();
      
    }
    
    
    // No parent commit defined for existing head
    if(state.getCommit().isPresent() && commitParent == null && !Boolean.TRUE.equals(parentIsLatest)) {
      return (CommitResult) ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Parent commit can only be undefined for the first commit!")
                  .append(" Parent commit for:")
                  .append(" '").append(gid).append("'")
                  .append(" is: '").append(state.getCommit().get().getId()).append("'!")
                  .toString())
              .build())
          .status(CommitStatus.ERROR)
          .build();
    }
    
    // Wrong parent commit
    if(state.getCommit().isPresent() && commitParent != null && 
        !commitParent.equals(state.getCommit().get().getId()) &&
        !Boolean.TRUE.equals(parentIsLatest)) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(gid).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(commitParent).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitStatus.ERROR)
          .build();
    }

    return null;
  }
  
  private static CommitStatus visitStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitStatus.CONFLICT;
    }
    return CommitStatus.ERROR;
    
  }
  
  interface CommitBatchBuilder {
    CommitBatchBuilder commitAuthor(String commitAuthor);
    CommitBatchBuilder commitMessage(String commitMessage);
    CommitBatchBuilder toBeInserted(Map<String, JsonObject> toBeInserted);
    CommitBatchBuilder toBeRemoved(Collection<String> toBeRemoved);
    Batch build();
  }
}
