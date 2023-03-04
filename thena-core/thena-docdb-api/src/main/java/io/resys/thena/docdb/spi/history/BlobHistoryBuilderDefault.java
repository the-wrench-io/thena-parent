package io.resys.thena.docdb.spi.history;

import java.util.HashMap;
import java.util.Map;

import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryBuilder;
import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryResult;
import io.resys.thena.docdb.api.actions.ImmutableBlobHistoryResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BlobHistoryBuilderDefault implements BlobHistoryBuilder {
  private final ClientState state;
  private final Map<String, String> criteria = new HashMap<>();
  private String repoName;
  private String headName;
  private boolean latest;
  private String blobName;
  
  @Override public BlobHistoryBuilder repo(String repoName, String headName) { this.repoName = repoName; this.headName = headName; return this; }
  @Override public BlobHistoryBuilder entry(String key, String value) { this.criteria.put(key, value); return this; }
  @Override public BlobHistoryBuilder blobName(String blobName) { this.blobName = blobName; return this; }
  @Override public BlobHistoryBuilder latestOnly() { this.latest = true; return this; }
  @Override public BlobHistoryBuilder latestOnly(boolean latest) { this.latest = latest; return this; }
  
  @Override
  public Uni<BlobHistoryResult> build() {
    RepoAssert.notEmpty(repoName, () -> "repoName is not defined!");
    RepoAssert.notEmpty(headName, () -> "headName is not defined!");
    
    return state.repos().getByNameOrId(repoName).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var ex = RepoException.builder().notRepoWithName(repoName);
        log.warn(ex.getText());
        return Uni.createFrom().item(ImmutableBlobHistoryResult
            .builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(ex)
            .build());
      }
      final var ctx = state.withRepo(existing);
      return ctx.query().blobHistory()
        .latestOnly(latest)
        .blobName(blobName)
        .criteria(criteria)
        .find().collect()
        .asList().onItem().transform(found -> ImmutableBlobHistoryResult
            .builder().status(ObjectsStatus.OK).values(found)
            .build());
    });
  }
}
