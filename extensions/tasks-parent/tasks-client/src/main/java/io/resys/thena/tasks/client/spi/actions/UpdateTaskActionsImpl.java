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

import io.resys.thena.tasks.client.api.actions.TaskActions.UpdateTaskActions;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskAction;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UpdateTaskActionsImpl implements UpdateTaskActions {

  private final DocumentStore ctx;

  @Override
  public Uni<Task> updateOne(TaskAction command) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<Task> updateOne(List<TaskAction> commands) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<List<Task>> updateMany(List<TaskAction> commands) {
    // TODO Auto-generated method stub
    return null;
  }
}

