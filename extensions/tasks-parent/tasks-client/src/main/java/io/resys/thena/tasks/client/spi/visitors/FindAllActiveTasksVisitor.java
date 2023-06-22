package io.resys.thena.tasks.client.spi.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefStateBuilder;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.RefVisitor;

public class FindAllActiveTasksVisitor implements RefVisitor<List<Task>> {
  @Override
  public RefStateBuilder start(DocumentConfig config, RefStateBuilder builder) {
    return builder.blobs()
        .blobCriteria(Arrays.asList(ImmutableBlobCriteria.builder()
        .key("documentType").value(Document.DocumentType.TASK.name())
        .type(CriteriaType.EXACT)
        .build()));
  }
  @Override
  public RefObjects visit(DocumentConfig config, ObjectsResult<RefObjects> envelope) {
    if(envelope.getStatus() != ObjectsStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Task> end(DocumentConfig config, RefObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    final var tree = ref.getTree();
    return tree.getValues().values().stream().map(treeValue -> {
      final var blobId = treeValue.getBlob();
      final var blob = ref.getBlobs().get(blobId);
      return blob.getValue().mapTo(ImmutableTask.class);
    }).collect(Collectors.toList());
  }
}
