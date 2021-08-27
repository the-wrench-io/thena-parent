package io.resys.thena.docdb.spi.sql.defaults;

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

import java.util.List;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.sql.ImmutableSql;
import io.resys.thena.docdb.spi.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.spi.sql.SqlBuilder.BlobSqlBuilder;
import io.resys.thena.docdb.spi.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.spi.sql.SqlBuilder.SqlTuple;
import io.vertx.mutiny.sqlclient.Tuple;

public class DefaultBlobSqlBuilder implements BlobSqlBuilder {
  
  
  private final ClientCollections options;
  
  public DefaultBlobSqlBuilder(ClientCollections options) {
    super();
    this.options = options;
  }
  @Override
  public Sql create() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getBlobs()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  value TEXT NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
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
  public SqlTuple findByIds(List<String> blobId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBlobs())
        .append(" WHERE id IN(?1)")
        .build())
        .props(Tuple.of(blobId))
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
        .append("(id, value) VALUES($1, $2)")
        .build())
        .props(Tuple.of(blob.getId(), blob.getValue()))
        .build();
  }
}
