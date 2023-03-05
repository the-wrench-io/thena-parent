package io.resys.thena.docdb.sql.queries;

import java.util.HashMap;
import java.util.Map;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.spi.ClientQuery.BlobHistoryQuery;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class BlobHistoryQuerySqlPool implements BlobHistoryQuery {

  private final ClientQuerySqlContext context;
  private boolean latestOnly;
  private String name;
  private Map<String, String> criteria = new HashMap<>();

  @Override public BlobHistoryQuery latestOnly(boolean latestOnly) { this.latestOnly = latestOnly; return this; }
  @Override public BlobHistoryQuery blobName(String name) { this.name = name; return this; }
  @Override public BlobHistoryQuery criteria(Map<String, String> criteria) { this.criteria.putAll(criteria); return this; }

  @Override
  public Multi<BlobHistory> find() {
    final var sql = context.getBuilder().blobs().findByCriteria(name, latestOnly, criteria);
    final var stream = context.getWrapper().getClient().preparedQuery(sql.getValue())
        .mapping(row -> context.getMapper().blobHistory(row));
    
    if(log.isDebugEnabled()) {
      log.debug("Blob history query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return (sql.getProps().size() > 0 ? stream.execute(sql.getProps()) : stream.execute())
        .onItem()
        .transformToMulti((RowSet<BlobHistory> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> context.getErrorHandler().deadEnd(
          new StringBuilder("Can't find 'BLOB'-s by 'name': '").append(name).append("'!").toString() 
          , e));
  }
  
}
