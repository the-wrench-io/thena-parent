package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.ImmutableBlobHistory;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
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
  @Override
  public BlobHistory blobHistory(Row row) { 
    return ImmutableBlobHistory.builder()
        .treeId(row.getString("tree"))
        .treeValueName(row.getString("blob_name"))
        .commit(row.getString("commit_id"))
        .blob(ImmutableBlob.builder()
            .id(row.getString("blob_id"))
            .value(row.getJsonObject("blob_value"))
            .build())
        .build();
  }
}
