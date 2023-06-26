package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryQuery;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.HistoryObjects;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocHistoryVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetArchivedTasksVisitor implements DocHistoryVisitor<Task> {

  private final @Nullable String likeTitle;
  private final @Nullable String likeDescription;
  private final @Nullable String reporterId;
  private final LocalDate fromCreatedOrUpdated;
  
  @Override
  public BlobHistoryQuery start(DocumentConfig config, BlobHistoryQuery builder) {
     builder.latestOnly(true).matchBy(
      MatchCriteria.equalsTo("documentType", Document.DocumentType.TASK.name()),
      MatchCriteria.notNull("archived")
      //TODO MatchCriteria.greaterThanOrEqualTo("updated", fromCreatedOrUpdated.atStartOfDay())
    );
     
     if(likeTitle != null) {
       builder.matchBy(MatchCriteria.like("title", likeTitle));
     }
     if(likeDescription != null) {
       builder.matchBy(MatchCriteria.like("description", likeDescription));
     }
     if(reporterId != null) {
       builder.matchBy(MatchCriteria.equalsTo("reporterId", reporterId));
     }
     
     return builder;
  }

  @Override
  public HistoryObjects visitEnvelope(DocumentConfig config, QueryEnvelope<HistoryObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ARCHIVED_TASKS_FAIL").add(config, envelope)
      .add(c -> c.addArgs(JsonObject.of(
          "fromCreatedOrUpdated", fromCreatedOrUpdated,
          "likeTitle", likeTitle,
          "likeDescription", likeDescription,
          "reporterId", reporterId
          ).encode()))
      .build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Task> end(DocumentConfig config, HistoryObjects values) {
    return values.accept(blob -> blob.mapTo(ImmutableTask.class));
  }
}
