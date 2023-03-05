package io.resys.thena.docdb.spi.pgsql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.sql.SqlBuilder.BlobSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.sql.statement.DefaultBlobSqlBuilder;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;


public class BlobSqlBuilderPg extends DefaultBlobSqlBuilder implements BlobSqlBuilder {
  
  public BlobSqlBuilderPg(ClientCollections options) {
    super(options);
  }
  
  @Override
  public SqlTuple insertOne(Blob blob) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBlobs())
        .append(" (id, value) VALUES($1, $2)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(Tuple.of(blob.getId(), blob.getValue()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<Blob> blobs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBlobs())
        .append(" (id, value) VALUES($1, $2)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(blobs.stream()
            .map(v -> Tuple.of(v.getId(), v.getValue()))
            .collect(Collectors.toList()))
        .build();
  }
  
/*

WITH RECURSIVE generation AS (
    SELECT id, parent, 0 AS order_no
    FROM nested_10_commits
    WHERE parent IS NULL
UNION ALL
    SELECT child.id, child.parent, order_no+1 AS order_no
    FROM nested_10_commits as child
    JOIN generation g ON g.id = child.parent
)
 
select 
  item.name,
  blobs.value,
  item.blob,
  item.tree,
  commit.parent,
  commit.id as commit_id,
  generation.order_no
from 
  nested_10_treeitems as item
  left join nested_10_commits as commit
  on(commit.tree = item.tree)
  left join generation on(generation.id = commit.id)
  left join nested_10_blobs as blobs on(blobs.id = item.blob)
where 
 blobs.value @> '{"bodyType": "PROJECT"}'
 */
  @Override
  public SqlTuple findByCriteria(String name, boolean latestOnly, Map<String, String> criteria) {
    
    final var criteriaString = new JsonObject();
    int paramIndex = 1;
    final var params = new LinkedList<>();
    for(final var entry : criteria.entrySet()) {
      final var parsed = new JsonObject();
      criteriaString.put(entry.getKey(), entry.getValue());
      //criteriaString.add(parsed);
    }
    
    final var sql = new SqlStatement()
    .append("WITH RECURSIVE generation AS (").ln()
    .append("    SELECT id, parent, 0 AS order_no").ln()
    .append("    FROM ").append(options.getCommits()).ln()
    .append("    WHERE parent IS NULL").ln()
    .append("UNION ALL").ln()
    .append("    SELECT child.id, child.parent, order_no+1 AS order_no").ln()
    .append("    FROM ").append(options.getCommits()).append(" as child").ln()
    .append("    JOIN generation g ON g.id = child.parent").ln()
    .append(")").ln()
    .append("SELECT ").ln()
    .append("  item.name as blob_name,").ln()
    .append("  blobs.value as blob_value,").ln()
    .append("  item.blob as blob_id,").ln()
    .append("  item.tree as tree,").ln()
    .append("  commit.parent as commit_parent,").ln()
    .append("  commit.id as commit_id,").ln()
    .append("  generation.order_no as order_no").ln()
    .append("FROM ").ln()
    .append("  ").append(options.getTreeItems()).append(" as item").ln()
    .append("  LEFT JOIN ").append(options.getCommits()).append(" AS commit").ln()
    .append("  ON(commit.tree = item.tree)").ln()
    .append("  LEFT JOIN generation ON(generation.id = commit.id)").ln()
    .append("  LEFT JOIN ").append(options.getBlobs()).append(" AS blobs ON(blobs.id = item.blob)").ln()
    .append("WHERE ").ln()
    
    .append("blobs.value @> '").append(criteriaString.toString()).append("'").ln()
    .build();
    
    return ImmutableSqlTuple.builder()
        .value(sql)
        .props(Tuple.of(params))
        .build();
  }
}
