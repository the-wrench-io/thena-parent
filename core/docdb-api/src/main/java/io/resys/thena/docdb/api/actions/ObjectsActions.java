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
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface ObjectsActions {

  RefStateBuilder refState();
  BlobStateBuilder blobState();
  
  interface BlobStateBuilder {
    BlobStateBuilder repo(String repoName);
    BlobStateBuilder anyId(String refOrCommitOrTag);
    BlobStateBuilder ref(String refId);
    BlobStateBuilder blobNames(List<String> blobName);
    BlobStateBuilder blobName(String blobName);
    BlobStateBuilder blobCriteria(List<BlobCriteria> blobCriteria);
    
    Uni<ObjectsResult<BlobObject>> get();
    Uni<ObjectsResult<BlobObjects>> list();
  }

  
  // build REF world state, no blobs by default
  interface RefStateBuilder {
    RefStateBuilder repo(String repoName);
    RefStateBuilder ref(String ref);
    RefStateBuilder blobs();
    RefStateBuilder blobs(boolean load);
    RefStateBuilder blobCriteria(List<BlobCriteria> blobCriteria);
    Uni<ObjectsResult<RefObjects>> build();
  }

  
  interface BlobContainer {
    <T> List<T> accept(BlobVisitor<T> visitor);
  }
  
  @FunctionalInterface
  interface BlobVisitor<T> {
    T visit(JsonObject blobValue);
  }
  
  @Value.Immutable
  interface BlobObject {
    Repo getRepo();
    Commit getCommit();
    //Tree getTree();
    Blob getBlob();
    
    default <T> T accept(BlobVisitor<T> visitor) {
      return visitor.visit(getBlob().getValue());
    }
  }

  @Value.Immutable
  interface BlobObjects extends BlobContainer {
    Repo getRepo();
    Commit getCommit();
    Tree getTree();
    List<Blob> getBlob();
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getBlob().stream()
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }

  @Value.Immutable
  interface RefObjects extends BlobContainer {
    Repo getRepo();
    Ref getRef();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
}
