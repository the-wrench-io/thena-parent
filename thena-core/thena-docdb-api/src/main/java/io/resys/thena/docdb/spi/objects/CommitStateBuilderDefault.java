package io.resys.thena.docdb.spi.objects;

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.actions.ImmutableCommitObjects;
import io.resys.thena.docdb.api.actions.ImmutableObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.CommitObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.CommitStateBuilder;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CommitStateBuilderDefault implements CommitStateBuilder {
  private final ClientState state;
  private final List<BlobCriteria> blobCriteria = new ArrayList<>();
  private String repo;
  private String anyId; //refOrCommitOrTag
  private boolean blobs;
  
  @Override public CommitStateBuilderDefault blobCriteria(List<BlobCriteria> blobCriteria) { this.blobCriteria.addAll(blobCriteria); return this; }
  @Override public CommitStateBuilder blobs() { this.blobs = true; return this; }
  
  @Override
  public Uni<ObjectsResult<CommitObjects>> build() {
    RepoAssert.notEmpty(repo, () -> "repo is not defined!");
    RepoAssert.notEmpty(anyId, () -> "refOrCommitOrTag is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var error = RepoException.builder().notRepoWithName(repo);
        log.error(error.getText());
        return Uni.createFrom().item(ImmutableObjectsResult
            .<CommitObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(error)
            .build());
      }
      final var ctx = state.withRepo(existing);
      
      return ObjectsUtils.findCommit(ctx, anyId)
        .onItem().transformToUni(commit -> {
          if(commit == null) {
            final var error = RepoException.builder().noCommit(existing, anyId);
            log.error(error.getText());
            return Uni.createFrom().item(ImmutableObjectsResult
                .<CommitObjects>builder()
                .status(ObjectsStatus.ERROR)
                .addMessages(error)
                .build()); 
          }
          return getState(existing, commit, ctx);
        });
    });
  }
  
  
  private Uni<ObjectsResult<CommitObjects>> getState(Repo repo, Commit commit, ClientRepoState ctx) {
    return ObjectsUtils.getTree(commit, ctx).onItem()
    .transformToUni(tree -> {
      if(this.blobs) {
        return getBlobs(tree, ctx, blobCriteria)
          .onItem().transform(blobs -> ImmutableObjectsResult.<CommitObjects>builder()
            .repo(repo)
            .objects(blobs
                .repo(repo)
                .tree(tree)
                .commit(commit)
                .build())
            .repo(repo)
            .status(ObjectsStatus.OK)
            .build());
      }
      
      return Uni.createFrom().item(ImmutableObjectsResult.<CommitObjects>builder()
        .repo(repo)
        .objects(ImmutableCommitObjects.builder()
            .repo(repo)
            .tree(tree)
            .commit(commit)
            .build())
        .status(ObjectsStatus.OK)
        .build());
    });
  }
  
  private static Uni<ImmutableCommitObjects.Builder> getBlobs(Tree tree, ClientRepoState ctx, List<BlobCriteria> blobCriteria) {
    return ctx.query().blobs().criteria(blobCriteria).findByTreeId(tree.getId())
        .collect().asList().onItem()
        .transform(blobs -> {
          final var objects = ImmutableCommitObjects.builder();
          blobs.forEach(blob -> objects.putBlobs(blob.getId(), blob));
          return objects;
        });
  }
}
