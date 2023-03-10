package io.resys.thena.tasks.api.actions;

/*-
 * #%L
 * thena-tasks-api
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.tasks.api.model.Task;
import io.resys.thena.tasks.api.model.Task.TaskComment;
import io.resys.thena.tasks.api.model.Task.TaskExtension;
import io.smallrye.mutiny.Uni;


public interface ChangeActions {
  Uni<Task> create(CreateTask command);
  Uni<List<Task>> create(List<CreateTask> commands);
  
  Uni<Task> updateOne(UpdateCommand ...command);
  Uni<Task> updateOne(List<UpdateCommand> commands);

  
  interface Command extends Serializable {
    String getUserId();
    @Nullable LocalDateTime getTargetDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateTask.class) @JsonDeserialize(as = ImmutableCreateTask.class)
  interface CreateTask extends Command {
    List<String> getAssigneeRoles();
    @Nullable String getAssigneeId();
    
    @Nullable LocalDate getDueDate();
    String getSubject();
    String getDescription();
    Integer getPriority();
    
    List<String> getLabels();
    List<TaskExtension> getExtensions();
    List<TaskComment> getExternalComments();
    List<TaskComment> getInternalComments();
  }

  
  interface UpdateCommand extends Command {
    String getUserId();
    String getTaskId();
    @Nullable LocalDateTime getTargetDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTask.class) @JsonDeserialize(as = ImmutableAssignTask.class)
  interface UpdateTaskComment extends UpdateCommand {
    @Nullable String getCommentId();
    @Nullable String getReplyToCommentId();
    String getCommentText();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTask.class) @JsonDeserialize(as = ImmutableAssignTask.class)
  interface AssignTask extends UpdateCommand {
    String getAssigneeId();
    @Nullable String getAssigneeRoles();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableAssignRoles.class) @JsonDeserialize(as = ImmutableAssignRoles.class)
  interface AssignRoles extends UpdateCommand {
    String getAssigneeRoles();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableCompleteTask.class) @JsonDeserialize(as = ImmutableCompleteTask.class)
  interface CompleteTask extends UpdateCommand {
    Task.Status getStatus();
    String getUserComment();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableUpdateTaskExtension.class) @JsonDeserialize(as = ImmutableUpdateTaskExtension.class)
  interface UpdateTaskExtension extends UpdateCommand {
    @Nullable String getId(); // create | update
    String getType();
    String getBody();
  }
}
