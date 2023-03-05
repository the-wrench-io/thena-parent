package io.resys.thena.docdb.spi.pgsql;

import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import io.resys.thena.docdb.sql.support.SqlStatement;

public class PgSqlSchema extends SqlSchemaImpl {
  public PgSqlSchema(ClientCollections options) {
    super(options);
  }

  @Override
  public Sql blobs() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getBlobs()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  value jsonb NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public SqlSchemaImpl withOptions(ClientCollections options) {
    return new PgSqlSchema(options);
  }
}
