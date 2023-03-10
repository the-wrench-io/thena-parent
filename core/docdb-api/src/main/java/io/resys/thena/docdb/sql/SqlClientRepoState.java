package io.resys.thena.docdb.sql;

import java.util.function.Function;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.ImmutableClientQuerySqlContext;
import io.resys.thena.docdb.sql.queries.ClientInsertBuilderSqlPool;
import io.resys.thena.docdb.sql.support.ImmutableSqlClientWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlClientRepoState implements ClientRepoState {
  private final ImmutableSqlClientWrapper wrapper;
  private final ErrorHandler handler; 
  private final Function<ClientCollections, SqlMapper> sqlMapper;
  private final Function<ClientCollections, SqlBuilder> sqlBuilder;
  private final Function<ClientQuerySqlContext, ClientQuery> clientQuery;
  
  @Override
  public String getRepoName() {
    return wrapper.getRepo().getName();
  }
  @Override
  public Repo getRepo() {
    return wrapper.getRepo();
  }
  @Override
  public ClientQuery query() {
    final var ctx = ImmutableClientQuerySqlContext.builder()
        .mapper(sqlMapper.apply(wrapper.getNames()))
        .builder(sqlBuilder.apply(wrapper.getNames()))
        .wrapper(wrapper)
        .errorHandler(handler)
        .build();
      
    return clientQuery.apply(ctx);
  }
  @Override
  public ClientInsertBuilder insert() {
    return new ClientInsertBuilderSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
}
