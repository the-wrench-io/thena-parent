package io.resys.thena.tasks.client.api.actions;

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
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskAction;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public interface TaskActions {

  CreateTaskActions create();
  UpdateTaskActions update();
  ActiveTaskActions active();
  DeleteTaskActions delete();

  interface CreateTaskActions {
    Uni<Task> createOne(CreateTask command);
    Uni<List<Task>> createMany(List<CreateTask> commands);
  }

  interface UpdateTaskActions {
    Uni<Task> updateOne(TaskAction command);
    Uni<Task> updateOne(List<TaskAction> commands);
    Uni<List<Task>> updateMany(List<TaskAction> commands);
  }

  interface DeleteTaskActions {
    Multi<Task> deleteAll();
  }

  interface ActiveTaskActions {
    Multi<Task> findAll();
    Multi<Task> findByRoles(List<String> roles);
    Multi<Task> findByAssignee(List<String> assignees);
    Uni<Task> get(String id);
  }
  
  interface Command extends Serializable {
    String getUserId();
    @Nullable LocalDateTime getTargetDate();
  }

}
