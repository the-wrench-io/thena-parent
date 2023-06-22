package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskHistory;
import io.resys.thena.tasks.client.api.model.Task;

import java.util.ArrayList;
import java.util.List;

public class HistoryVisitor {

  private final List<BlobHistory> collectedHistory = new ArrayList<>();
  private BlobHistory previous = null;
  private final ImmutableTaskHistory.Builder historyBuilder = ImmutableTaskHistory.builder();

  public HistoryVisitor visitBlobHistory(List<BlobHistory> history) {
    history.forEach(this::visitBlobHistory);
    return this;
  }

  private void visitBlobHistory(BlobHistory history) {
    if(previous != null && previous.getBlob().equals(history.getBlob())) {
      previous = history;
      return;
    }

    historyBuilder.addVersions(history.getBlob().getValue().mapTo(ImmutableTask.class));
    previous = history;
  }

  public Task.TaskHistory build() {
    return historyBuilder.id("not implemented yet").build();
  }

}
