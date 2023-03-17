package io.resys.thena.docdb.api.actions;

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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface CommitActions {
  CommitBuilder builder();
  CommitQuery query();
  CommitStateBuilder state();
  
  interface CommitQuery {
    CommitQuery repoName(String repoId); // head GID to what to append
    Uni<List<Commit>> findAllCommits();
    Uni<List<Tree>> findAllCommitTrees();
  }
  
  interface CommitBuilder {
    CommitBuilder id(String headGid); // head GID to what to append
    CommitBuilder parent(String parentCommit); // for validations
    CommitBuilder parentIsLatest();
    CommitBuilder head(String repoId, String headName); // head GID to what to append
    CommitBuilder append(String name, JsonObject blob);
    CommitBuilder merge(String name, JsonObjectMerge blob);
    CommitBuilder remove(String name);
    CommitBuilder remove(List<String> name);
    CommitBuilder author(String author);
    CommitBuilder message(String message);
    Uni<CommitResult> build();
  }
  
  // build REF world state, no blobs by default
  interface CommitStateBuilder {
    CommitStateBuilder repo(String repoName);
    CommitStateBuilder anyId(String refOrCommitOrTag);
    CommitStateBuilder blobs();
    CommitStateBuilder blobs(boolean load);
    CommitStateBuilder blobCriteria(List<BlobCriteria> blobCriteria);
    Uni<ObjectsResult<CommitObjects>> build();
  }

  @FunctionalInterface
  interface JsonObjectMerge {
    JsonObject apply(JsonObject previousState);
  }
  
  enum CommitStatus { OK, ERROR, CONFLICT }
  
  @Value.Immutable
  interface CommitResult {
    String getGid(); // repo/head
    @Nullable
    Commit getCommit();
    CommitStatus getStatus();
    List<Message> getMessages();
  }
  
  @Value.Immutable
  interface CommitObjects {
    Repo getRepo();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
  }

}
