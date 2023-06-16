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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.tasks.client.api.actions.ExportActions;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.Export;
import io.resys.thena.tasks.client.api.model.ImmutableExport;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.resys.thena.tasks.client.spi.visitors.ExportVisitor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class ExportActionsImpl implements ExportActions {
  private final DocumentStore ctx;

  @Override
  public ExportQuery export() {
    return new ExportQueryImpl(ctx);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true, chain = true)
  @Getter(AccessLevel.NONE)
  @Data
  private static class ExportQueryImpl implements ExportQuery {
    private final DocumentStore ctx;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime targetDate;

    @Override
    public Uni<Export> build() {
      RepoAssert.notEmpty(name, () -> "name must be defined!");
      RepoAssert.notNull(startDate, () -> "startDate must be defined!");
      RepoAssert.notNull(endDate, () -> "endDate must be defined!");
      RepoAssert.isTrue(!endDate.isBefore(startDate), () -> "endDate cannot be before startDate!");
      
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
      
      return query.onItem().transform(this::mapQueryForTree)
          .onItem().transform((List<Task> tasks) -> {
            
            final var events = new ExportVisitor().visitTasks(tasks).build();
            return ImmutableExport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .name(name)
                .created(targetDate)
                .id("not implemented yet")
                .hash("not implemented yet")
                .events(events)
                .build();
          });

    }
    
    
    private List<Task> mapQueryForTree(ObjectsResult<RefObjects> state) {
      if(state.getStatus() != ObjectsStatus.OK) {
        final var config = ctx.getConfig();
        throw new DocumentStoreException("FIND_ALL_TASKS_FOR_EXPORT_FAIL", ImmutableDocumentExceptionMsg.builder()
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
}
