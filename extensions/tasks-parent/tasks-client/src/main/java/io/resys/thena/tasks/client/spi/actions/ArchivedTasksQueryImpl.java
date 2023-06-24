package io.resys.thena.tasks.client.spi.actions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryResult;
import io.resys.thena.docdb.api.actions.ImmutableMatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.tasks.client.api.actions.TaskActions.ArchivedTasksQuery;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ArchivedTasksQueryImpl implements ArchivedTasksQuery {
  private final DocumentStore ctx;

  @Override
  public ArchivedTasksQuery title(String likeTitle) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery description(String likeDescription) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery reporterId(String reporterId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery status(List<Status> status) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery status(Status... status) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery assignees(List<String> assignees) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery assignees(String... assignees) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery roles(List<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArchivedTasksQuery roles(String... roles) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<List<Task>> build() {
    final var config = ctx.getConfig();
    return config.getClient()
        .history()
        .blobQuery()
        .head(config.getProjectName(), config.getHeadName())
        .latestOnly(true)
        .matchBy(Arrays.asList(
            ImmutableMatchCriteria.builder()
              .key("documentType").value(Document.DocumentType.TASK.name())
              .type(MatchCriteriaType.EQUALS)
              .build(),
            ImmutableMatchCriteria.builder()
              .key("archived")
              .type(MatchCriteriaType.NOT_NULL)
              .build()
            ))
        .get()
        .onItem().transform(this::map)
        ;
  }

  
  private List<Task> map(BlobHistoryResult response) {
    if(response.getStatus() != QueryEnvelopeStatus.OK) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_ARCHIVED_TASKS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(response.getRepo() == null ? config.getProjectName() : response.getRepo().getName())
          .value(response.getRepo() == null ? "no-repo" : response.getRepo().getId())
          .addAllArgs(response.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final List<BlobHistory> history = response.getValues();

    return history.stream()
        .map(item -> item.getBlob().getValue())
        .map(json -> json.mapTo(ImmutableTask.class))
        .collect(Collectors.toUnmodifiableList());
  }
}
