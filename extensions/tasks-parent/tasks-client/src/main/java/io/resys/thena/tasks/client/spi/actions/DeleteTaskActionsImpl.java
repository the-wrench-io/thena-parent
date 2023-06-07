package io.resys.thena.tasks.client.spi.actions;

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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.tasks.client.api.actions.TaskActions.DeleteTaskActions;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeleteTaskActionsImpl implements DeleteTaskActions {
  private final DocumentStore ctx;
  
  
  @Override
  public Multi<Task> deleteAll() {
    final var config = ctx.getConfig();
    final var client = config.getClient();
    final var query = client
        .objects().refState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobs()
        .blobCriteria(Arrays.asList(ImmutableBlobCriteria.builder()
            .key("documentType").value(Document.DocumentType.TASK.name())
            .type(CriteriaType.EXACT)
            .build()))
        .build();
    
    return query.onItem().transform(this::mapQuery)
        .onItem().transformToUni(items -> client.commit().builder()
          .head(config.getRepoName(), config.getHeadName())
          .message("Delete all tasks")
          .parentIsLatest()
          .remove(items.stream().map(item -> item.getId()).toList())
          .author(config.getAuthor().get())
          .build().onItem().transform(commit -> {
            if(commit.getStatus() == CommitStatus.OK) {
              return items;
            }
            throw new DocumentStoreException("DELETE_FAIL", DocumentStoreException.convertMessages(commit));
          })
        )
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }


  private List<Task> mapQuery(ObjectsResult<RefObjects> state) {
    if(state.getStatus() != ObjectsStatus.OK) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_ALL_TASKS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final var objects = state.getObjects();
    if(objects == null) {
      return Collections.emptyList();
    }
    
    final var tree = objects.getTree();
    return tree.getValues().values().stream().map(treeValue -> mapTree(state, treeValue)).collect(Collectors.toList());
  }
  
  private Task mapTree(ObjectsResult<RefObjects> state, TreeValue treeValue) {
    final var blobId = treeValue.getBlob();
    final var blob = state.getObjects().getBlobs().get(blobId);
    return blob.getValue().mapTo(ImmutableTask.class);
  }

}
