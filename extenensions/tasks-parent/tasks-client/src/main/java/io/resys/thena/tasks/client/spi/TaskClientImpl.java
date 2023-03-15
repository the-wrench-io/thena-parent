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
import io.resys.thena.tasks.client.api.actions.ChangeActions;
import io.resys.thena.tasks.client.api.actions.QueryActions;
import io.resys.thena.tasks.client.api.actions.StatisticsActions;
import io.resys.thena.tasks.client.spi.changes.ChangeActionsImpl;
import io.resys.thena.tasks.client.spi.query.QueryActionsImpl;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TasksClient {
  private final DocumentStore ctx;
  
  @Override
  public ChangeActions changes() {
    return new ChangeActionsImpl(ctx);
  }

  @Override
  public QueryActions query() {
    return new QueryActionsImpl(ctx);
  }

  @Override
  public StatisticsActions statistics() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TaskRepositoryQuery repo() {
    DocumentStore.RepositoryQuery next = ctx.repo();
    return new TaskRepositoryQuery() {
      @Override public TaskRepositoryQuery repoName(String repoName) { next.repoName(repoName); return this; }
      @Override public TaskRepositoryQuery headName(String headName) { next.headName(headName); return this; }
      @Override public Uni<Boolean> createIfNot() { return next.createIfNot(); }
      @Override public Uni<TasksClient> create() { return next.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public TasksClient build() { return new TaskClientImpl(next.build()); }
    };
  }
}
