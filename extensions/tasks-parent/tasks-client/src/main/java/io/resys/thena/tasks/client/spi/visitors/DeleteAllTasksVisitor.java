package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObjects;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocCommitVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllTasksVisitor implements DocCommitVisitor<Task>{

  private final String userId;
  private final LocalDateTime targetDate;
  
  private CommitBuilder archiveCommand;
  private CommitBuilder removeCommand;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery query) {
    // Create two commands: one for making changes by adding archive flag, the other for deleting task from commit tree
    this.archiveCommand = visitCommitCommand(config).message("Archive tasks");
    this.removeCommand = visitCommitCommand(config).message("Delete tasks");
    
    // Build the blob criteria for finding all documents of type task
    return query.matchBy(
          MatchCriteria.equalsTo("documentType", Document.DocumentType.TASK.name())
    );
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Task>> end(DocumentConfig config, PullObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tasksRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((CommitResult commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((CommitResult commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tasksRemoved);
  }

  
  
  private CommitBuilder visitCommitCommand(DocumentConfig config) {
    final var client = config.getClient();
    return client.commit().commitBuilder()
      .head(config.getProjectName(), config.getHeadName())
      .latestCommit()
      .author(config.getAuthor().get());
  }
  
  
  private List<Task> visitTree(PullObjects state) {
    return state.getBlob().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableTask.class))
      .map(task -> visitTask(task))
      .collect(Collectors.toUnmodifiableList());
  }
  private Task visitTask(Task currentVersion) {
    final var taskId = currentVersion.getId();
    
    final var nextVersion = ImmutableTask.builder().from(currentVersion)
        .version(userId)
        .archived(targetDate)
        .addTransactions(ImmutableTaskTransaction.builder()
            .id(String.valueOf(currentVersion.getTransactions().size() +1))
            .addCommands(ImmutableArchiveTask.builder()
                .taskId(taskId)
                .userId(userId)
                .targetDate(targetDate)
                .build())
            .build())
        .build();
    final var json = JsonObject.mapFrom(nextVersion);
    archiveCommand.append(taskId, json);    
    removeCommand.remove(taskId);
    return nextVersion;
  }

}
