package io.resys.thena.docdb.sql.defaults;

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

import java.util.Arrays;

import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.CommitSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.vertx.mutiny.sqlclient.Tuple;

public class DefaultCommitSqlBuilder implements CommitSqlBuilder {

  private final ClientCollections options;
  
  public DefaultCommitSqlBuilder(ClientCollections options) {
    super();
    this.options = options;
  }
  @Override
  public Sql create() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(255) NOT NULL,").ln()
    .append("  tree VARCHAR(40) NOT NULL,").ln()
    .append("  parent VARCHAR(40),").ln()
    .append("  merge VARCHAR(40)").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public Sql constraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_PARENT_FK").ln()
        .append("  FOREIGN KEY (parent)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_TREE_FK").ln()
        .append("  FOREIGN KEY (tree)").ln()
        .append("  REFERENCES ").append(options.getTrees()).append(" (id);").ln()
        
        .append("CREATE INDEX ").append(options.getCommits()).append("_TREE_INDEX")
        .append(" ON ").append(options.getTreeItems()).append(" (tree);").ln()
        
        .append("CREATE INDEX ").append(options.getCommits()).append("_PARENT_INDEX")
        .append(" ON ").append(options.getTreeItems()).append(" (tree);").ln()
        .build())
        .build();
  }
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Commit commit) {
    
    var message = commit.getMessage();
    if(commit.getMessage().length() > 100) {
      message = message.substring(0, 100);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getCommits())
        .append(" (id, datetime, author, message, tree, parent, merge) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(Tuple.from(Arrays.asList(
            commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
            commit.getTree(), commit.getParent().orElse(null), commit.getMerge().orElse(null))))
        .build();
  }
  
  
}
