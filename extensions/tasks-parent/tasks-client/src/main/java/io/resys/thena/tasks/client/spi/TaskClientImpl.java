package io.resys.thena.tasks.client.spi;

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

import io.resys.thena.tasks.client.api.TasksClient;
import io.resys.thena.tasks.client.api.actions.StatisticsActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.api.actions.TaskActions.ActiveTaskActions;
import io.resys.thena.tasks.client.api.actions.TaskActions.CreateTaskActions;
import io.resys.thena.tasks.client.api.actions.TaskActions.DeleteTaskActions;
import io.resys.thena.tasks.client.api.actions.TaskActions.UpdateTaskActions;
import io.resys.thena.tasks.client.spi.actions.CreateTaskActionsImpl;
import io.resys.thena.tasks.client.spi.actions.TaskActionsImpl;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TasksClient {
  private final DocumentStore ctx;
  
  @Override
  public TaskActions actions() {
    return new TaskActionsImpl(ctx);
  }

  @Override
  public StatisticsActions statistics() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TaskRepositoryQuery repo() {
    DocumentStore.RepositoryQuery repo = ctx.repo();
    return new TaskRepositoryQuery() {
      @Override public TaskRepositoryQuery repoName(String repoName) { repo.repoName(repoName); return this; }
      @Override public TaskRepositoryQuery headName(String headName) { repo.headName(headName); return this; }
      @Override public Uni<Boolean> createIfNot() { return repo.createIfNot(); }
      @Override public Uni<TasksClient> create() { return repo.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public TasksClient build() { return new TaskClientImpl(repo.build()); }
    };
  }
}
