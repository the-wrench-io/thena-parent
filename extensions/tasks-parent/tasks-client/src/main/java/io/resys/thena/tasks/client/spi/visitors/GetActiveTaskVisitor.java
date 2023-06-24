package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobStateBuilder;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocBlobVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveTaskVisitor implements DocBlobVisitor<Task> {
  private final String id;
  
  @Override
  public BlobStateBuilder start(DocumentConfig config, BlobStateBuilder builder) {
    return builder.blobName(id);
  }

  @Override
  public BlobObject visit(DocumentConfig config, ObjectsResult<BlobObject> envelope) {
    if(envelope.getStatus() != ObjectsStatus.OK) {
      throw DocumentStoreException.builder("GET_TASK_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TASK_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Task end(DocumentConfig config, BlobObject blob) {
    return blob.accept((JsonObject json) -> json.mapTo(ImmutableTask.class));
  }
}
