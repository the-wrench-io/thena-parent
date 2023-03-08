package io.resys.thena.docdb.sql.statement;

import java.util.ArrayList;

/*-
 * #%L
 * thena-docdb-pgsql
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.sql.SqlBuilder.BlobSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultBlobSqlBuilder implements BlobSqlBuilder {
  protected final ClientCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String blobId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(blobId))
        .build();
  }
  @Override
  public SqlTuple findByIds(Collection<String> blobId, List<BlobCriteria> criteria) {
    final var conditions = createWhereCriteria(criteria);
    final var tuple = new ArrayList<>(conditions.getProps());
    
    final var ides = new StringBuilder();
    int index = conditions.getProps().size() + 1;
    for(final var id : blobId) {
      if(ides.length() > 0) {
        ides.append(" OR ");
      }
      ides.append(" id = $").append(index++);
      tuple.add(id);
    }
    if(!ides.isEmpty()) {
      ides.insert(0, "(").append(")");
    }
    
    final var where = new StringBuilder(conditions.getValue());
    if(!ides.isEmpty()) {
      if(!where.isEmpty()) {
        where.append(" AND ");
      }
      where.append(ides.toString());
    }
    
    if(!where.isEmpty()) {
      where.insert(0, " WHERE ");
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blob.* FROM ").append(options.getBlobs()).append(" as blob ").ln()
        .append(where.toString())
        .build())
        .props(Tuple.from(tuple))
        .build();
  }
  @Override
  public SqlTuple findByTree(String treeId, List<BlobCriteria> criteria) {
    final var conditions = createWhereCriteria(criteria);
    final var props = new LinkedList<>(conditions.getProps());
    props.add(treeId);
    final var treeIdPos = props.size() + 1;
    
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blob.* ").ln()
        .append("  FROM ").append(options.getBlobs()).append(" AS blob ").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
        .append("  ON blob.id = item.blob").ln()
        .append("  WHERE ").ln()
        .append("  ").append(conditions.getValue()).ln()
        .append("  item.tree = $").append(String.valueOf(treeIdPos)).ln()
        .append(" ")
        .build())
        .props(Tuple.of(treeId))
        .build();
  }
  @Override
  public SqlTuple insertOne(Blob blob) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBlobs())
        .append(" (id, value) VALUES($1, $2)")
        .append(" ON CONFLICT (id) DO NOTHING")
        .build())
        .props(Tuple.of(blob.getId(), blob.getValue().encode()))
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
            .map(v -> Tuple.of(v.getId(), v.getValue().encode()))
            .collect(Collectors.toList()))
        .build();
  }
  
  
/**

WITH RECURSIVE generation AS (
    SELECT id, parent, 0 AS order_no
    FROM nested_10_commits
    WHERE parent IS NULL
UNION ALL
    SELECT child.id, child.parent, order_no+1 AS order_no
    FROM nested_10_commits as child
    JOIN generation g ON g.id = child.parent
)
SELECT * 
FROM ( SELECT 
  blob_name,
  blob_value,
  blob_id,
  tree,
  commit_parent,
  commit_id,
  order_no
  RANK() OVER (PARTITION BY blob_name, order_no ORDER BY order_no DESC) AS RANK
FROM (SELECT 
  item.name as blob_name,
  blobs.value as blob_value,
  item.blob as blob_id,
  item.tree as tree,
  commit.parent as commit_parent,
  commit.id as commit_id,
  generation.order_no as order_no
FROM 
  nested_10_treeItems as item
  LEFT JOIN nested_10_commits AS commit
  ON(commit.tree = item.tree)
  LEFT JOIN generation ON(generation.id = commit.id)
  LEFT JOIN nested_10_blobs AS blobs ON(blobs.id = item.blob)
WHERE blobs.value LIKE $1
)
) WHERE RANK = 1

   */
  @Override
  public SqlTuple find(String name, boolean latestOnly, List<BlobCriteria> criteria) {

    final String sql;
    final var conditions = createWhereCriteria(criteria);
    final var where = new StringBuilder(conditions.getValue());
    if(!where.isEmpty()) {
      where.insert(0, "WHERE ");
    }

    
    if(latestOnly) {
      final var fromData = new SqlStatement()
          .append(createRecursionSelect())
          .append(where.toString()).ln()
          .build();
      sql = new SqlStatement().append(createRecursion()).append(createLatest(fromData)).build();
    } else {
      sql = new SqlStatement()
          .append(createRecursion())
          .append(createRecursionSelect())
          .append(where.toString()).ln()
          .build();      
    }
    return ImmutableSqlTuple.builder()
        .value(sql)
        .props(Tuple.from(conditions.getProps()))
        .build();
  }
  
  @RequiredArgsConstructor @lombok.Data
  private static class WhereSqlFragment {
    private final String value;
    private final List<Object> props;
  }
  
  private WhereSqlFragment createWhereCriteria(List<BlobCriteria> criteria) {

    final var where = new StringBuilder();
    
    int paramIndex = 1;
    final var props = new LinkedList<>();
    for(final var entry : criteria) {
      if(paramIndex > 1) {
        where.append(" AND ");        
      }
      where.append("blobs.value LIKE $").append(paramIndex++);
      var param = new StringBuilder()
          .append("\"").append(entry.getKey()).append("\"")
          .append(":");
      
      if(entry.getType() == CriteriaType.LIKE) {
        param.append("\"%").append(entry.getValue()).append("%\"");
      } else {
        param.append("\"").append(entry.getValue()).append("\"");
      }
      props.add(param.insert(0, "%").append("%").toString());
    }
    return new WhereSqlFragment(where.toString(), props);
  }
  

  protected String createLatest(String fromData) {
    return new SqlStatement()
        .append("SELECT ranked.* ").ln()
        .append("FROM ( SELECT ").ln()
        .append("  RANK() OVER (PARTITION BY blob_name ORDER BY order_no DESC) AS RANK,").ln()
        .append("  by_commit.*").ln()
        .append("FROM (").append(fromData).append(") as by_commit").ln()
        .append(") as ranked WHERE ranked.RANK = 1").ln()
        .toString();           
  }
  
  protected String createRecursionSelect() {
    return new SqlStatement()
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
        .toString();       
  }
  
  protected String createRecursion() {
    return new SqlStatement()
        .append("WITH RECURSIVE generation AS (").ln()
        .append("    SELECT id, parent, 0 AS order_no").ln()
        .append("    FROM ").append(options.getCommits()).ln()
        .append("    WHERE parent IS NULL").ln()
        .append("UNION ALL").ln()
        .append("    SELECT child.id, child.parent, order_no+1 AS order_no").ln()
        .append("    FROM ").append(options.getCommits()).append(" as child").ln()
        .append("    JOIN generation g ON g.id = child.parent").ln()
        .append(")").ln().toString();       
  }
}
