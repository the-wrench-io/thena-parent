package io.resys.thena.docdb.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.smallrye.mutiny.Uni;

public interface HistoryActions {

  BlobHistoryBuilder blob();
  
  interface BlobHistoryBuilder {
    BlobHistoryBuilder repo(String repo, String headName);
    BlobHistoryBuilder criteria(BlobCriteria ... criteria);
    BlobHistoryBuilder criteria(List<BlobCriteria> criteria);

    BlobHistoryBuilder blobName(String blobName); // entity name
    BlobHistoryBuilder latestOnly(); // search only from last known version
    BlobHistoryBuilder latestOnly(boolean latest); // search only from last known version
    Uni<BlobHistoryResult> build();
  }
  
  @Value.Immutable
  interface BlobHistoryResult {
    List<BlobHistory> getValues();
    
    @Nullable Repo getRepo();    
    ObjectsStatus getStatus();
    List<Message> getMessages();
  }

}
