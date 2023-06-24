package io.resys.thena.docdb.sql;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.api.models.ThenaObject.Tree;
import io.resys.thena.docdb.api.models.ThenaObject.TreeValue;
import io.resys.thena.docdb.spi.support.DataMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public interface SqlMapper extends DataMapper<Row>{
  Repo repo(Row row);
  Commit commit(Row row);
  Tree tree(Row row);
  TreeValue treeItem(Row row);
  Tag tag(Row row);
  Branch ref(Row row);
  Blob blob(Row row);
  BlobHistory blobHistory(Row row);
  JsonObject jsonObject(Row row, String columnName);
}
