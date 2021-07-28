package io.resys.thena.docdb.spi.repo;

import io.resys.thena.docdb.api.actions.RepoActions;
import io.resys.thena.docdb.api.actions.RepoActions.QueryBuilder;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class RepoQueryBuilder implements RepoActions.QueryBuilder {

  private final ClientState state;
  private String id;
  private String rev;
  
  public RepoQueryBuilder(ClientState state) {
    super();
    this.state = state;
  }
  
  @Override
  public RepoActions.QueryBuilder id(String id) {
    this.id = id;
    return this;
  }

  @Override
  public QueryBuilder rev(String rev) {
    this.rev = rev;
    return this;
  }

  @Override
  public Multi<Repo> find() {
   return state.repos().find(); 
  }

  @Override
  public Uni<Repo> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.repos().getByNameOrId(id);
  }
}
