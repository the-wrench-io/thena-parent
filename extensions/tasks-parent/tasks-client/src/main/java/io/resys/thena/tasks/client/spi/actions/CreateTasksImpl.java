package io.resys.thena.tasks.client.spi.actions;

import java.util.Arrays;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.CreateTasks;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.CreateTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateTasksImpl implements CreateTasks {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> createOne(CreateTask command) {
    return this.createMany(Arrays.asList(command))
       .onItem().transform(tasks -> tasks.get(0)) ;
  }
  
  @Override
  public Uni<List<Task>> createMany(List<CreateTask> commands) {
    return ctx.getConfig().accept(new CreateTasksVisitor(commands));
  }

}
