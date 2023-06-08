package io.resys.thena.tasks.client.api.actions;

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
import io.resys.thena.tasks.client.api.model.TaskAction;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public interface TaskActions {

  CreateTasks createTask();
  UpdateTasks updateTask();
  ActiveTaskQuery queryActiveTasks();

  interface CreateTasks {
    Uni<Task> createOne(CreateTask command);
    Uni<List<Task>> createMany(List<CreateTask> commands);
  }

  interface UpdateTasks {
    Uni<Task> updateOne(TaskAction command);
    Uni<Task> updateOne(List<TaskAction> commands);
    Uni<List<Task>> updateMany(List<TaskAction> commands);
  }

  interface ActiveTaskQuery {
    Multi<Task> findAll();
    Multi<Task> findByRoles(List<String> roles);
    Multi<Task> findByAssignee(List<String> assignees);
    Uni<Task> get(String id);
    Multi<Task> deleteAll();
  }
  
  /* 
   * NEEDED INTERFACES
   * 
   * export / import data 
   * query historic data
   * task transactions
   *  - multiple actions can be performed on a task at once, such as updating status, owner, priority, etc.
   *  - if these are saved as one whole, the actions are part of a transaction (group of actions)
   *  - interface will dictate the date of the transaction
   *  - will exist to group actions together 
   *  - is needed because without it, you can't tell which actions were part of a transaction or which were simply individual actions
  
  */
}
