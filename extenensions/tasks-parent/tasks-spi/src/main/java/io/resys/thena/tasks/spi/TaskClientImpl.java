package io.resys.thena.tasks.spi;

import io.resys.thena.tasks.api.TaskClient;
import io.resys.thena.tasks.api.actions.ChangeActions;
import io.resys.thena.tasks.api.actions.QueryActions;
import io.resys.thena.tasks.api.actions.StatisticsActions;
import io.resys.thena.tasks.spi.changes.ChangeActionsImpl;
import io.resys.thena.tasks.spi.query.QueryActionsImpl;
import io.resys.thena.tasks.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {
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
      @Override public Uni<TaskClient> create() { return next.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public TaskClient build() { return new TaskClientImpl(next.build()); }
    };
  }
}
