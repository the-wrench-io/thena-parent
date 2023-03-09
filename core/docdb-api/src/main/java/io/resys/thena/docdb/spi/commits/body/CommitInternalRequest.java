package io.resys.thena.docdb.spi.commits.body;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.Repo;
import io.vertx.core.json.JsonObject;

@lombok.Data @lombok.Builder
public class CommitInternalRequest {
  private Optional<RefObjects> parent;
  private Repo repo;
  private String ref;
  private String commitAuthor;
  private String commitMessage;
  private Map<String, JsonObject> append;
  private Collection<String> remove;
}
