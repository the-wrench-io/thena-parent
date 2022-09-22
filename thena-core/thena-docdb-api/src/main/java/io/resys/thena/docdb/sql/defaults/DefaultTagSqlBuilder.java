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

import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.TagSqlBuilder;
import io.vertx.mutiny.sqlclient.Tuple;

public class DefaultTagSqlBuilder implements TagSqlBuilder {
  
  private final ClientCollections options;
  
  public DefaultTagSqlBuilder(ClientCollections options) {
    super();
    this.options = options;
  }
  
  @Override
  public Sql create() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTags()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit VARCHAR(40) NOT NULL,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(100) NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  @Override
  public Sql constraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getTags()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTags()).append("_TAG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        .build())
        .build();
  }
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" WHERE name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public SqlTuple deleteByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getTags())
        .append(" WHERE name = $1")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }
  @Override
  public SqlTuple insertOne(Tag newTag) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTags())
        .append(" (id, commit, datetime, author, message) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(newTag.getName(), newTag.getCommit(), newTag.getDateTime().toString(), newTag.getAuthor(), newTag.getMessage()))
        .build();
  }
}
