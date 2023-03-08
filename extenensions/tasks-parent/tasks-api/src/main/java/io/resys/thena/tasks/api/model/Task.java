package io.resys.thena.tasks.api.model;

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

@Value.Immutable @JsonSerialize(as = ImmutableTask.class) @JsonDeserialize(as = ImmutableTask.class)
public interface Task {
  String getId();
  String getVersion();
  
  TaskStatusEvent getCreated();
  
  @Nullable TaskStatusEvent getCompleted();
  @Nullable TaskStatusEvent getUpdated();
  
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
  
  enum Status { CREATED, COMPLETED, REJECTED }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskStatusEvent.class) @JsonDeserialize(as = ImmutableTaskStatusEvent.class)
  interface TaskStatusEvent extends Serializable {
    LocalDateTime getDateTime();
    String getUserId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskExtension.class) @JsonDeserialize(as = ImmutableTaskExtension.class)
  interface TaskExtension extends Serializable {
    String getId();
    String getType();
    String getBody();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTaskComment.class) @JsonDeserialize(as = ImmutableTaskComment.class)
  interface TaskComment {
    String getId();
    LocalDateTime getCreated();
    @Nullable String getReplyToId();
    String getCommentText();
    String getUsername();
  }
}
