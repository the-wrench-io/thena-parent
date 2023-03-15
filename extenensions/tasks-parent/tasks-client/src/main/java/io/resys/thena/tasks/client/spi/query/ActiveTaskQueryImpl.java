package io.resys.thena.tasks.client.spi.query;

/*-
 * #%L
 * thena-tasks-client
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.tasks.client.api.actions.QueryActions.ActiveTaskQuery;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTaskQueryImpl implements ActiveTaskQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> get(String id) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Uni<List<Task>> findByRoles(List<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Uni<List<Task>> findByAssignee(List<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Uni<List<Task>> findAll() {
    final var config = ctx.getConfig();
    final var query = config.getClient()
        .objects().refState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobs()
        .blobCriteria(Arrays.asList(ImmutableBlobCriteria.builder()
            .key("documentType").value(Document.DocumentType.TASK.name())
            .type(CriteriaType.EXACT)
            .build()))
        .build();
    
    return query.onItem().transform(this::map);
  }


  private List<Task> map(ObjectsResult<RefObjects> state) {
    if(state.getStatus() != ObjectsStatus.OK) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_ALL_TASKS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final var tree = state.getObjects().getTree();
    return tree.getValues().values().stream().map(treeValue -> map(state, treeValue)).collect(Collectors.toList());
  }
  
  private Task map(ObjectsResult<RefObjects> state, TreeValue treeValue) {
    final var blobId = treeValue.getBlob();
    final var blob = state.getObjects().getBlobs().get(blobId);
    return blob.getValue().mapTo(ImmutableTask.class);
  }
}
