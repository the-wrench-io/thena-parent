package io.resys.thena.docdb.spi.objects;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectsUtils {
  public static Uni<String> getTagCommit(String tagName, ClientRepoState ctx) {
    return ctx.query().tags().name(tagName).get()
        .onItem().transform(tag -> tag == null ? null : tag.getCommit());
  }
  public static Uni<String> getRefCommit(String refName, ClientRepoState ctx) {
    return ctx.query().refs().name(refName)
        .onItem().transform(ref -> ref == null ? null : ref.getCommit());
  }
  public static Uni<Tree> getTree(Commit commit, ClientRepoState ctx) {
    return ctx.query().trees().id(commit.getTree());
  }
  public static Uni<Commit> getCommit(String commit, ClientRepoState ctx) {
    return ctx.query().commits().id(commit);
  }
  public static Uni<Commit> findCommit(ClientRepoState ctx, String anyId) {
    
    return ObjectsUtils.getTagCommit(anyId, ctx)
      .onItem().transformToUni(tag -> {
        if(tag == null) {
          log.info("Can't find from repo: '{}' a tag: '{}' trying to find by ref: '{}'", ctx.getRepoName(), anyId, anyId);
          return ObjectsUtils.getRefCommit(anyId, ctx);
        }
        return Uni.createFrom().item(tag);
      })
      .onItem().transformToUni(commitId -> {
        if(commitId == null) {
          log.info("Can't find from repo: '{}' a ref: '{}' trying to find by commit: '{}'", ctx.getRepoName(), anyId, anyId);
          return ObjectsUtils.getCommit(anyId, ctx);
        }
        return ObjectsUtils.getCommit(commitId, ctx);
      });
  }
  

  public static Uni<Map<String, Blob>> getBlobs(Tree tree, List<BlobCriteria> blobCriteria, ClientRepoState ctx) {
    return ctx.query().blobs().criteria(blobCriteria).findByTreeId(tree.getId()).collect().asList().onItem()
        .transform(blobs -> blobs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
  }
}
