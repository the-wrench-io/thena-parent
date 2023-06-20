package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryVisitor {

  private final List<BlobHistory> collectedHistory = new ArrayList<>();
  private BlobHistory previous = null;
  private final DocumentConfig config;

  public HistoryVisitor (DocumentStore ctx) {
    this.config = ctx.getConfig();
  }

  public HistoryVisitor visitTaskHistory(HistoryActions.BlobHistoryResult historyResult, String taskId) {
    if(historyResult.getStatus() != ObjectsResult.ObjectsStatus.OK) {
      throw new DocumentStoreException("FIND_HISTORY_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(historyResult.getRepo() == null ? config.getRepoName() : historyResult.getRepo().getName())
          .value(historyResult.getRepo() == null ? "no-repo" : historyResult.getRepo().getId())
          .addAllArgs(historyResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build());
    }

    this.visitBlobHistory(historyResult.getValues());

    if(collectedHistory.isEmpty()) {
      throw new DocumentStoreException("FIND_TASK_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(historyResult.getRepo() == null ? config.getRepoName() : historyResult.getRepo().getName())
          .value(historyResult.getRepo() == null ? "no-repo" : historyResult.getRepo().getId())
          .addAllArgs(List.of("Task with id: " + taskId + " not found!"))
          .build());
    }
    return this;
  }

  private void visitBlobHistory(List<BlobHistory> history) {
    history.forEach(this::visitBlobHistory);
  }

  private void visitBlobHistory(BlobHistory history) {
    if(previous == null) {
      collectedHistory.add(history);
      previous = history;
      return;
    }

    if(history.getBlob().equals(previous.getBlob())) {
      previous = history;
      return;
    }

    collectedHistory.add(history);
    previous = history;
  }

  public List<Task> build() {
    return collectedHistory.stream().map(item -> item.getBlob().getValue().mapTo(ImmutableTask.class))
        .collect(Collectors.toUnmodifiableList());
  }

}
