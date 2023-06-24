package io.resys.thena.tasks.client.spi.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.BranchActions.BranchObjectsQuery;
import io.resys.thena.docdb.api.actions.ImmutableMatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.BranchObjects;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocBranchVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;

public class FindAllActiveTasksVisitor implements DocBranchVisitor<List<Task>> {
  @Override
  public BranchObjectsQuery start(DocumentConfig config, BranchObjectsQuery builder) {
    return builder.docsIncluded()
        .matchBy(Arrays.asList(ImmutableMatchCriteria.builder()
        .key("documentType").value(Document.DocumentType.TASK.name())
        .type(MatchCriteriaType.EQUALS)
        .build()));
  }
  @Override
  public BranchObjects visitEnvelope(DocumentConfig config, QueryEnvelope<BranchObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Task> end(DocumentConfig config, BranchObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((JsonObject json) -> json.mapTo(ImmutableTask.class));
  }
}
