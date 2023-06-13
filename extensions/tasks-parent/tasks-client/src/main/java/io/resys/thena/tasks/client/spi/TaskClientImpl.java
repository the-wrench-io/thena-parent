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
import io.resys.thena.tasks.client.api.actions.MigrationActions;
import io.resys.thena.tasks.client.api.actions.RepositoryQuery;
import io.resys.thena.tasks.client.api.actions.StatisticsActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.spi.actions.TaskActionsImpl;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TasksClient {
  private final DocumentStore ctx;
  
  @Override
  public TaskActions tasks() {
    return new TaskActionsImpl(ctx);
  }

  @Override
  public StatisticsActions statistics() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryQuery repo() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.repo();
    return new RepositoryQuery() {
      @Override public RepositoryQuery repoName(String repoName) { repo.repoName(repoName); return this; }
      @Override public RepositoryQuery headName(String headName) { repo.headName(headName); return this; }
      @Override public Uni<Boolean> createIfNot() { return repo.createIfNot(); }
      @Override public Uni<TasksClient> create() { return repo.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public TasksClient build() { return new TaskClientImpl(repo.build()); }
    };
  }

  @Override
  public MigrationActions migrate() {
    // TODO Auto-generated method stub
    return null;
  }
}
