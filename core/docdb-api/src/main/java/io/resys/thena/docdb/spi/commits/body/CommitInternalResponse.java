package io.resys.thena.docdb.spi.commits.body;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.Repo;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface CommitInternalResponse {
  CommitResponseStatus getStatus();
  Repo getRepo();
  Message getLog();
  Ref getRef();
  Commit getCommit();
  Tree getTree();
  Collection<Blob> getBlobs();
  List<Message> getMessages();
  

  @Value.Immutable
  interface RedundentCommitTree {
    boolean isEmpty();
    Map<String, TreeValue> getTreeValues();
    Map<String, Blob> getBlobs();
    String getLog();
  }
  
  @Value.Immutable
  public interface RedundentHashedBlob {
    String getName();
    String getHash();
    JsonObject getBlob();
  }
  public enum CommitResponseStatus {
    OK, EMPTY, ERROR, CONFLICT
  }
}