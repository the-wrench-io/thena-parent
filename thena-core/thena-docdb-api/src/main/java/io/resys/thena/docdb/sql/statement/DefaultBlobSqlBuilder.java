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
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientCollections;
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
  public SqlTuple findByIds(Collection<String> blobId) {
    StringBuilder params = new StringBuilder();
    List<String> tuple = new ArrayList<>();
    
    int index = 1;
    for(final var id : blobId) {
      if(params.length() == 0) {
        params.append(" WHERE ");
      } else {
        params.append(" OR ");
      }
      params.append(" id = $").append(index++);
      tuple.add(id);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .append(params.toString())
        .build())
        .props(Tuple.from(tuple))
        .build();
  }
  @Override
  public SqlTuple findByTree(Tree tree) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT blob.* ").ln()
        .append("  FROM ").append(options.getBlobs()).append(" AS blob ").ln()
        .append("  LEFT JOIN ").append(options.getTreeItems()).append(" AS item ").ln()
        .append("  ON blob.id = item.blob").ln()
        .append("  WHERE item.tree = $1").ln()
        .append(" ")
        .build())
        .props(Tuple.of(tree.getId()))
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
    final var criteriaString = new StringBuilder();
    int paramIndex = 1;
    final var params = new LinkedList<>();
    for(final var entry : criteria.entrySet()) {
      if(paramIndex > 1) {
        criteriaString.append(" AND ");        
      }
      criteriaString
        .append("blobs.value LIKE $").append(paramIndex++);
      params.add(new StringBuilder()
          .append("%")
          .append("\"").append(entry.getKey()).append("\"")
          .append(":")
          .append("\"%").append(entry.getValue()).append("%\"")
          .append("%")
          .toString());
    }
    
    if(!criteriaString.isEmpty()) {
      criteriaString.insert(0, "WHERE ");
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
    .append(criteriaString.toString()).ln()
    .build();
    
    return ImmutableSqlTuple.builder()
        .value(sql)
        .props(Tuple.from(params))
        .build();
  }
}
