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

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.smallrye.mutiny.Uni;

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

  @Value.Immutable
  interface BlobObject {
    Repo getRepo();
    Commit getCommit();
    //Tree getTree();
    Blob getBlob();
  }

  @Value.Immutable
  interface BlobObjects {
    Repo getRepo();
    Commit getCommit();
    Tree getTree();
    List<Blob> getBlob();
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


  @Value.Immutable
  interface RefObjects {
    Repo getRepo();
    Ref getRef();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
  }
  

}
