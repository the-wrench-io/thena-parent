package io.resys.thena.docdb.spi.pgsql;

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.factories.SqlMapperImpl;
import io.vertx.mutiny.sqlclient.Row;


public class SqlMapperPg extends SqlMapperImpl implements SqlMapper {

  public SqlMapperPg(ClientCollections ctx) {
    super(ctx);
  }
  @Override
  public Blob blob(Row row) {
    return ImmutableBlob.builder()
        .id(row.getString("id"))
        .value(row.getJsonObject("value"))
        .build();
  }
}
