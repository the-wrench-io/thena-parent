package io.resys.thena.docdb.spi.objects;

import io.resys.thena.docdb.api.actions.BranchActions;
import io.resys.thena.docdb.spi.ClientState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BranchActionsImpl implements BranchActions {
  private final ClientState state;

  @Override
  public BranchObjectsQuery branchQuery() {
    return new BranchObjectsQueryImpl(state);
  }
}
