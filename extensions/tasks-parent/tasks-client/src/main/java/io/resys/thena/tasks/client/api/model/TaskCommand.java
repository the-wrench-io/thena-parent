package io.resys.thena.tasks.client.api.model;

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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.Task.TaskComment;
import io.resys.thena.tasks.client.api.model.Task.TaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskCommandType;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateTask.class, name = "CreateTask"),
  @Type(value = ImmutableChangeTaskStatus.class, name = "ChangeTaskStatus"),
  @Type(value = ImmutableChangeTaskPriority.class, name = "ChangeTaskPriority"),
  @Type(value = ImmutableAssignTaskReporter.class, name = "AssignTaskReporter"),
})
public interface TaskCommand extends Serializable {
  String getUserId();
  @Nullable LocalDateTime getTargetDate();
  TaskCommandType getCommandType();
  
  
  enum TaskCommandType {
    CreateTask, ChangeTaskStatus, ChangeTaskPriority, AssignTaskReporter
  }
  
  interface TaskUpdateCommand extends TaskCommand {
    String getTaskId();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateTask.class) @JsonDeserialize(as = ImmutableCreateTask.class)
  interface CreateTask extends TaskCommand {
    List<String> getRoles();
    List<String> getAssigneeIds();
    String getReporterId();
    
    @Nullable Status getStatus();
    @Nullable LocalDate getDueDate();
    String getTitle();
    String getDescription();
    Priority getPriority();
    
    List<String> getLabels();
    List<TaskExtension> getExtensions();
    List<TaskComment> getComments();
    
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CreateTask; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskReporter.class) @JsonDeserialize(as = ImmutableAssignTaskReporter.class)
  interface AssignTaskReporter extends TaskUpdateCommand {
    String getReporterId();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AssignTaskReporter; }

  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskStatus.class) @JsonDeserialize(as = ImmutableChangeTaskStatus.class)
  interface ChangeTaskStatus extends TaskUpdateCommand {
    Task.Status getStatus();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskStatus; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskPriority.class) @JsonDeserialize(as = ImmutableChangeTaskPriority.class)
  interface ChangeTaskPriority extends TaskUpdateCommand {
    Priority getPriority();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskPriority; }
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskParent.class) @JsonDeserialize(as = ImmutableAssignTaskParent.class)
  interface AssignTaskParent extends TaskUpdateCommand {
    @Nullable String getParentId(); 
  }
    
  @Value.Immutable @JsonSerialize(as = ImmutableCommentOnTask.class) @JsonDeserialize(as = ImmutableCommentOnTask.class)
  interface CommentOnTask extends TaskUpdateCommand {
    @Nullable String getCommentId();
    @Nullable String getReplyToCommentId();
    String getCommentText();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskRoles.class) @JsonDeserialize(as = ImmutableAssignTaskRoles.class)
  interface AssignTaskRoles extends TaskUpdateCommand {
    String getRoles();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAssignTask.class) @JsonDeserialize(as = ImmutableAssignTask.class)
  interface AssignTask extends TaskUpdateCommand {
    String getAssigneeIds();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskDueDate.class) @JsonDeserialize(as = ImmutableChangeTaskDueDate.class)
  interface ChangeTaskDueDate extends TaskUpdateCommand {
    Optional<LocalDate> getDueDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskInfo.class) @JsonDeserialize(as = ImmutableChangeTaskInfo.class)
  interface ChangeTaskInfo extends TaskUpdateCommand {
    String getSubject();
    String getDescription();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskExtension.class) @JsonDeserialize(as = ImmutableChangeTaskExtension.class)
  interface ChangeTaskExtension extends TaskUpdateCommand {
    @Nullable String getId(); // create | update
    String getType();
    String getBody();
  }
}
