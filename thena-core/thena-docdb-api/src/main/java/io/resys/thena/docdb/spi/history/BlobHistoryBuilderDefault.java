package io.resys.thena.docdb.spi.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryBuilder;
import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryResult;
import io.resys.thena.docdb.api.actions.ImmutableBlobHistoryResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data @Accessors(fluent = true)
@RequiredArgsConstructor
public class BlobHistoryBuilderDefault implements BlobHistoryBuilder {
  private final ClientState state;
  private final List<BlobCriteria> criteria = new ArrayList<>();
  private String repoName;
  private String headName;
  private boolean latestOnly;
  private String blobName;

  @Override public BlobHistoryBuilder criteria(BlobCriteria ... criteria) { this.criteria.addAll(Arrays.asList(criteria)); return this; }
  @Override public BlobHistoryBuilder criteria(List<BlobCriteria> criteria) { this.criteria.addAll(criteria); return this; }
  @Override public BlobHistoryBuilder repo(String repoName, String headName) { this.repoName = repoName; this.headName = headName; return this; }
  @Override public BlobHistoryBuilder latestOnly() { this.latestOnly = true; return this; }
  
  @Override
  public Uni<BlobHistoryResult> build() {
    RepoAssert.notEmpty(repoName, () -> "repoName is not defined!");
    RepoAssert.notEmpty(headName, () -> "headName is not defined!");
    
    return state.repos().getByNameOrId(repoName).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var ex = RepoException.builder().notRepoWithName(repoName);
        log.error(ex.getText());
        return Uni.createFrom().item(ImmutableBlobHistoryResult
            .builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(ex)
            .build());
      }
      final var ctx = state.withRepo(existing);
      return ctx.query().blobHistory()
        .latestOnly(latestOnly)
        .blobName(blobName)
        .criteria(criteria)
        .find().collect()
        .asList().onItem().transform(found -> ImmutableBlobHistoryResult
            .builder().status(ObjectsStatus.OK).values(found)
            .build());
    });
  }
}
