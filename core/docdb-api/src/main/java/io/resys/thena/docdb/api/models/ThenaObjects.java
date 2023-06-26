package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.BlobContainer.BlobVisitor;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.IsObject;
import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.api.models.ThenaObject.Tree;

public interface ThenaObjects { 
  @Value.Immutable
  public interface ProjectObjects extends ThenaObjects {
    Map<String, Branch> getBranches();
    Map<String, Tag> getTags();
    Map<String, IsObject> getValues();   
  }

  
  @Value.Immutable
  interface CommitObjects extends ThenaObjects, BlobContainer  {
    Repo getRepo();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
  @Value.Immutable
  interface PullObject extends ThenaObjects {
    Repo getRepo();
    Commit getCommit();
    //Tree getTree();
    Blob getBlob();
    
    default <T> T accept(BlobVisitor<T> visitor) {
      return visitor.visit(getBlob().getValue());
    }
  }
  

  @Value.Immutable
  interface PullObjects extends BlobContainer, ThenaObjects {
    Repo getRepo();
    Commit getCommit();
    //Tree getTree();
    List<Blob> getBlob();
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getBlob().stream()
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  @Value.Immutable
  interface BranchObjects extends BlobContainer, ThenaObjects {
    Repo getRepo();
    Branch getRef();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
  @Value.Immutable
  interface HistoryObjects extends BlobContainer, ThenaObjects {
    List<BlobHistory> getValues();
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getValues().stream()
          .map(value -> value.getBlob())
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
}
