package io.resys.thena.tasks.client.api.actions;

import java.time.LocalDate;

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

import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.TaskHistory;
import io.resys.thena.tasks.client.api.model.TaskAction;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public interface TaskActions {

  CreateTasks createTask();
  UpdateTasks updateTask();
  ActiveTasksQuery queryActiveTasks();

  interface CreateTasks {
    Uni<Task> createOne(CreateTask command);
    Uni<List<Task>> createMany(List<CreateTask> commands);
  }

  interface UpdateTasks {
    Uni<Task> updateOne(TaskAction command);
    Uni<Task> updateOne(List<TaskAction> commands);
    Uni<List<Task>> updateMany(List<TaskAction> commands);
  }

  interface ActiveTasksQuery {
    Multi<Task> findAll();
    Multi<Task> findByRoles(List<String> roles);
    Multi<Task> findByAssignee(List<String> assignees);
    Uni<Task> get(String id);
    Multi<Task> deleteAll();
  }
  
  interface ArchivedTasksQuery {
    ArchivedTasksQuery title(String likeTitle); // like == doesn't need to match exactly. If a title contains "bob", then it can be matched by bob
    ArchivedTasksQuery description(String likeDescription);
    ArchivedTasksQuery reporterId(String reporterId);
    ArchivedTasksQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);
    ArchivedTasksQuery status(List<Task.Status> status);
    ArchivedTasksQuery status(Task.Status ... status);
    ArchivedTasksQuery assignees(List<String> assignees);
    ArchivedTasksQuery assignees(String ... assignees);
    ArchivedTasksQuery roles(List<String> roles);
    ArchivedTasksQuery roles(String ... roles);
    Uni<List<Task>> find();
  }
  
  interface TaskHistoryQuery {
    Uni<TaskHistory> get(String taskId); 
  }
  
}
