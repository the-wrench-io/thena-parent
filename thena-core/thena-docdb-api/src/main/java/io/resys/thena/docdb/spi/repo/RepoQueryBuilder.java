package io.resys.thena.docdb.spi.repo;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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
