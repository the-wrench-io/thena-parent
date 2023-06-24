package io.resys.thena.tasks.client.spi.actions;

import java.util.ArrayList;
import java.util.Arrays;

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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.tasks.client.api.actions.TaskActions.ActiveTasksQuery;
import io.resys.thena.tasks.client.api.actions.TaskActions.UpdateTasks;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.visitors.UpdateTaskVisitor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateTasksImpl implements UpdateTasks {

  private final DocumentStore ctx;
  private final Supplier<ActiveTasksQuery> query;

  @Override
  public Uni<Task> updateOne(TaskUpdateCommand command) {        
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<Task> updateOne(List<TaskUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    final var uniqueTaskIds = commands.stream().map(command -> command.getTaskId()).distinct().collect(Collectors.toList());
    RepoAssert.isTrue(uniqueTaskIds.size() == 1, () -> "Task id-s must be same, but got: %s!", uniqueTaskIds);
    
    return this.query.get().get(uniqueTaskIds.get(0))
        .onItem().transform(taskBeforeUpdate -> new UpdateTaskVisitor(taskBeforeUpdate).visit(commands).build())
        .onItem().transformToUni(taskAfterUpdate -> {
          final var json = JsonObject.mapFrom(taskAfterUpdate);
          final var config = ctx.getConfig();
          return config.getClient().commit().builder()
              .head(config.getRepoName(), config.getHeadName())
              .message("Update task")
              .parentIsLatest()
              .append(taskAfterUpdate.getId(), json)
              .author(config.getAuthor().get())
              .build().onItem().transform(commit -> {
                if(commit.getStatus() == CommitStatus.OK) {
                  return taskAfterUpdate;
                }
                throw new DocumentStoreException("TASK_UPDATE_FAIL", json, DocumentStoreException.convertMessages(commit));
              });          
        });
  }

  @Override
  public Uni<List<Task>> updateMany(List<TaskUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    final Map<String, List<TaskUpdateCommand>> commandByTaskId = commands.stream()
        .collect(Collectors.groupingBy(TaskUpdateCommand::getTaskId));

    final Multi<Task> tasks = this.query.get()
        .findByTaskIds(commandByTaskId.keySet())
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()))
        .onItem().transform((Task taskBeforeUpdate) -> new UpdateTaskVisitor(taskBeforeUpdate).visit(commands).build());
    
    return tasks.collect().asList()
        .onItem().transformToUni(tasksAfterUpdate -> {
          
          final var config = ctx.getConfig();
          final var commitBuilder = config.getClient().commit().builder()
            .head(config.getRepoName(), config.getHeadName())
            .message("Update tasks")
            .parentIsLatest()
            .author(config.getAuthor().get());
          
          final List<JsonObject> blobs = new ArrayList<>();
          for(final Task taskAfterUpdate : tasksAfterUpdate) {
            final var json = JsonObject.mapFrom(taskAfterUpdate);
            commitBuilder.append(taskAfterUpdate.getId(), json);
          }

          return commitBuilder.build().onItem().transform(commit -> {
                if(commit.getStatus() == CommitStatus.OK) {
                  return tasksAfterUpdate;
                }
                throw new DocumentStoreException("TASKS_UPDATE_FAIL", JsonObject.of("failedUpdates", blobs), DocumentStoreException.convertMessages(commit));
              });          
        });
  }
}

