package io.resys.thena.tasks.client.spi.actions;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.ActiveTasksQuery;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.DeleteAllTasksVisitor;
import io.resys.thena.tasks.client.spi.visitors.FindAllActiveTasksVisitor;
import io.resys.thena.tasks.client.spi.visitors.GetActiveTaskVisitor;
import io.resys.thena.tasks.client.spi.visitors.GetActiveTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTasksQueryImpl implements ActiveTasksQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> get(String id) {
    return ctx.getConfig().accept(new GetActiveTaskVisitor(id));
  }
  
  @Override
  public Uni<List<Task>> findAll() {
    return ctx.getConfig().accept(new FindAllActiveTasksVisitor());
  }

  @Override
  public Uni<List<Task>> deleteAll(String userId, LocalDateTime targetDate) {
    return ctx.getConfig().accept(new DeleteAllTasksVisitor(userId, targetDate))
        .onItem().transformToUni((response) -> response.getDeleteCommand().onItem().transform(deleted -> response.getValues()));
  }
  
  @Override
  public Uni<List<Task>> findByTaskIds(Collection<String> taskIds) {
    return ctx.getConfig().accept(new GetActiveTasksVisitor(taskIds));
  }
  


  @Override
  public Uni<List<Task>> findByRoles(Collection<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Uni<List<Task>> findByAssignee(Collection<String> assignees) {
    // TODO Auto-generated method stub
    return null;
  }
}
