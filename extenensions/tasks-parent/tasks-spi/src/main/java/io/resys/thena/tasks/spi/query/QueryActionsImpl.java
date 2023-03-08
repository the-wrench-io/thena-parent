package io.resys.thena.tasks.spi.query;

import io.resys.thena.tasks.api.actions.QueryActions;
import io.resys.thena.tasks.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class QueryActionsImpl implements QueryActions {
  private final DocumentStore ctx;
  
  @Override
  public ActiveTaskQuery active() {
    return new ActiveTaskQueryImpl(ctx);
  }
}
