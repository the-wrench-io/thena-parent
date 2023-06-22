package io.resys.thena.tasks.client.spi.actions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.tasks.client.api.actions.TaskActions.ActiveTasksQuery;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.resys.thena.tasks.client.spi.visitors.DeleteAllTasksVisitor;
import io.resys.thena.tasks.client.spi.visitors.FindAllActiveTasksVisitor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTasksQueryImpl implements ActiveTasksQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> get(String id) {
    final var config = ctx.getConfig();
    final Uni<ObjectsResult<BlobObject>> query = config.getClient()
        .objects().blobState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobName(id)
        .get();
    
    return query.onItem().transform(this::mapQueryForBlob);
  }
  
  @Override
  public Multi<Task> findAll() {
    return ctx.getConfig().accept(new FindAllActiveTasksVisitor())
    .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }

  @Override
  public Multi<Task> deleteAll(String userId, LocalDateTime targetDate) {
    return ctx.getConfig().accept(new DeleteAllTasksVisitor(userId, targetDate))
        .onItem().transformToUni((response) -> response.getDeleteCommand().onItem().transform(deleted -> response.getValues()))
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }

  
  private Task mapQueryForBlob(ObjectsResult<BlobObject> state) {
    if(state.getStatus() != ObjectsStatus.OK) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_ALL_TASKS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final BlobObject objects = state.getObjects();
    if(objects == null) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_TASKS_BY_ID_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final var blob = objects.getBlob();
    return blob.getValue().mapTo(ImmutableTask.class);
  }
  
  private List<Task> mapQueryForBlobs(ObjectsResult<BlobObjects> state) {
    if(state.getStatus() != ObjectsStatus.OK) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_ALL_TASKS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    
    final BlobObjects objects = state.getObjects();
    if(objects == null) {
      final var config = ctx.getConfig();
      throw new DocumentStoreException("FIND_TASKS_BY_IDS_FAIL", ImmutableDocumentExceptionMsg.builder()
          .id(state.getRepo() == null ? config.getRepoName() : state.getRepo().getName())
          .value(state.getRepo() == null ? "no-repo" : state.getRepo().getId())
          .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
          .build()); 
    }
    return objects.getBlob().stream().map(blob -> blob.getValue().mapTo(ImmutableTask.class))
        .collect(Collectors.toList());
  }

  @Override
  public Multi<Task> findByRoles(Collection<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Multi<Task> findByAssignee(Collection<String> assignees) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Multi<Task> findByTaskIds(Collection<String> taskIds) {
    final var config = ctx.getConfig();
    final var query = config.getClient()
        .objects().blobState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobNames(new ArrayList<>(taskIds))
        .list();
    
    return query.onItem().transform(this::mapQueryForBlobs).onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }
}
