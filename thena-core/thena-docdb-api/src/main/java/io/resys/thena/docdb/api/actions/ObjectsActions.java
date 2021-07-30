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
import io.resys.thena.docdb.api.models.Objects;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.smallrye.mutiny.Uni;

public interface ObjectsActions {
  RepoStateBuilder repoState();
  RefStateBuilder refState();
  CommitStateBuilder commitState();
  BlobStateBuilder blobState();
  
  interface BlobStateBuilder {
    BlobStateBuilder repo(String repoName);
    BlobStateBuilder anyId(String refOrCommitOrTag);
    BlobStateBuilder blobName(String blobName);
    Uni<ObjectsResult<BlobObjects>> build();
  }
  
  // build world state
  interface RepoStateBuilder {
    RepoStateBuilder repo(String repoName);
    Uni<ObjectsResult<Objects>> build();
  }

  // build REF world state, no blobs by default
  interface CommitStateBuilder {
    CommitStateBuilder repo(String repoName);
    CommitStateBuilder anyId(String refOrCommitOrTag);
    CommitStateBuilder blobs();
    CommitStateBuilder blobs(boolean load);
    Uni<ObjectsResult<CommitObjects>> build();
  }
  
  // build REF world state, no blobs by default
  interface RefStateBuilder {
    RefStateBuilder repo(String repoName);
    RefStateBuilder ref(String ref);
    RefStateBuilder blobs();
    RefStateBuilder blobs(boolean load);
    Uni<ObjectsResult<RefObjects>> build();
  }

  @Value.Immutable
  interface CommitObjects {
    Repo getRepo();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
  }

  @Value.Immutable
  interface RefObjects {
    Repo getRepo();
    Ref getRef();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
  }
  
  @Value.Immutable
  interface BlobObjects {
    Repo getRepo();
// TODO::    Ref getRef();
    Commit getCommit();
    Tree getTree();
    Blob getBlob();
  }
  
  enum ObjectsStatus {
    OK, ERROR
  }
  
  @Value.Immutable
  interface ObjectsResult<T> {
    @Nullable
    Repo getRepo();    
    @Nullable
    T getObjects();
    ObjectsStatus getStatus();
    List<Message> getMessages();
  }
}
