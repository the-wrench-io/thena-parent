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

import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.sql.ImmutableSql;
import io.resys.thena.docdb.spi.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.spi.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.spi.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.spi.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.spi.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.spi.sql.SqlBuilder.TreeItemSqlBuilder;
import io.vertx.mutiny.sqlclient.Tuple;


public class DefaultTreeItemSqlBuilder implements TreeItemSqlBuilder {

  private final ClientCollections options;
  
  public DefaultTreeItemSqlBuilder(ClientCollections options) {
    super();
    this.options = options;
  }
  @Override
  public Sql create() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTreeItems())
    .append("(")
    .append("  id SERIAL PRIMARY KEY,")
    .append("  name VARCHAR(255) NOT NULL,")
    .append("  blob VARCHAR(40) NOT NULL,")
    .append("  tree VARCHAR(40) NOT NULL")
    .append(");")
    .build()).build();
  }
  @Override
  public Sql constraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getTreeItems()).ln()
        .append("  ADD CONSTRAINT TREE_ITEM_BLOB_FK").ln()
        .append("  FOREIGN KEY (blob)").ln()
        .append("  REFERENCES ").append(options.getBlobs()).append(" (id);").ln()
        .append("ALTER TABLE ").append(options.getTreeItems()).ln()
        .append("  ADD CONSTRAINT TREE_ITEM_PARENT_FK").ln()
        .append("  FOREIGN KEY (tree)").ln()
        .append("  REFERENCES ").append(options.getTrees()).append(" (id);").ln()
        .build())
        .build();
  }
  
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTreeItems())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByTreeId(String treeId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getTreeItems()).ln()
        .append("  WHERE tree = $1").ln()
        .build())
        .props(Tuple.of(treeId))
        .build();
  }
  @Override
  public SqlTuple insertOne(Tree tree, TreeValue item) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTreeItems())
        .append(" (name, blob, tree) VALUES($1, $2, $3)")
        .build())
        .props(Tuple.of(item.getName(), item.getBlob(), tree.getId()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Tree item) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTreeItems())
        .append(" (name, blob, tree) VALUES($1, $2, $3)")
        .build())
        .props(item.getValues().values().stream()
            .map(v -> Tuple.of(v.getName(), v.getBlob(), item.getId()))
            .collect(Collectors.toList()))
        .build();
  }
}
