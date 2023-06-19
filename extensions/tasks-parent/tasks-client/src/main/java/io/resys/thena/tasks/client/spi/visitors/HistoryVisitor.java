package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.docdb.api.models.Objects.BlobHistory;

import java.util.ArrayList;
import java.util.List;

public class HistoryVisitor {

  private final List<BlobHistory> collected = new ArrayList<>();
  private BlobHistory previous = null;

  public HistoryVisitor visitBlobHistory(List<BlobHistory> history) {
    history.forEach(this::visitBlobHistory);
    return this;
  }

  private void visitBlobHistory(BlobHistory history) {
    if(previous == null) {
      collected.add(history);
      previous = history;
      return;
    }

    if(history.getBlob().equals(previous.getBlob())) {
      previous = history;
      return;
    }

    collected.add(history);
    previous = history;
  }

  public List<BlobHistory> build() {
    return collected;
  }

}
