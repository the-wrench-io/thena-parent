package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefStateBuilder;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.RefVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.visitors.DeleteAllTasksVisitor.DeleteAllTasksResult;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllTasksVisitor implements RefVisitor<DeleteAllTasksResult>{

  @Data @Builder
  public static class DeleteAllTasksResult {
    private List<Task> values;
    private Uni<CommitResult> deleteCommand;
  }
  private final String userId;
  private final LocalDateTime targetDate;
  
  private CommitBuilder archiveCommand;
  private CommitBuilder removeCommand;
  
  @Override
  public RefStateBuilder start(DocumentConfig config, RefStateBuilder builder) {
    // Create two commands one for making changes by adding archive flag, the other for deleting task from commit tree
    this.archiveCommand = visitCommitCommand(config).message("Archive tasks");
    this.removeCommand = visitCommitCommand(config).message("Delete tasks");
    
    // Build the blob criteria for finding all documents of type task
    return builder.blobs()
      .blobCriteria(Arrays.asList(ImmutableBlobCriteria.builder()
          .key("documentType").value(Document.DocumentType.TASK.name())
          .type(CriteriaType.EXACT)
          .build()));
  }

  @Override
  public RefObjects visit(DocumentConfig config, ObjectsResult<RefObjects> envelope) {
    if(envelope.getStatus() != ObjectsStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public DeleteAllTasksResult end(DocumentConfig config, RefObjects ref) {
    if(ref == null) {
      return DeleteAllTasksResult.builder()
          .values(Collections.emptyList())
          .deleteCommand(Uni.createFrom().nullItem())
          .build();
    }

    final var tasksRemoved = visitTree(ref, ref.getTree());    
    final var deleteCommand = archiveCommand.build()
      .onItem().transform(this::visitArchiveCommit)
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform(this::visitRemoveCommit);

    return DeleteAllTasksResult.builder()
        .values(tasksRemoved)
        .deleteCommand(deleteCommand)
        .build();
  }

  private CommitResult visitArchiveCommit(CommitResult commit) {
    if(commit.getStatus() == CommitStatus.OK) {
      return commit;
    }
    throw new DocumentStoreException("ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
  }
  
  private CommitResult visitRemoveCommit(CommitResult commit) {
    if(commit.getStatus() == CommitStatus.OK) {
      return commit;
    }
    throw new DocumentStoreException("REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
  }
  
  
  private CommitBuilder visitCommitCommand(DocumentConfig config) {
    final var client = config.getClient();
    return client.commit().builder()
      .head(config.getRepoName(), config.getHeadName())
      .parentIsLatest()
      .author(config.getAuthor().get());
  }
  
  
  private List<Task> visitTree(RefObjects state, Tree tree) {
    return tree.getValues().values().stream()
      .map(treeValue -> visitTreeValue(state, treeValue))
      .map(task -> visitTask(task))
      .collect(Collectors.toUnmodifiableList());
  }
  
  private Task visitTreeValue(RefObjects state, TreeValue value) {
    final var blobId = value.getBlob();
    final var blob = state.getBlobs().get(blobId);
    return blob.getValue().mapTo(ImmutableTask.class);
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
