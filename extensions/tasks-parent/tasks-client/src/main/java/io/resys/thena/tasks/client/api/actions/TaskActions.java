package io.resys.thena.tasks.client.api.actions;

import java.util.List;

import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskAction;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public interface TaskActions {

  CreateTaskActions create();
  UpdateTaskActions update();
  ActiveTaskActions active();
  DeleteTaskActions delete();

  interface CreateTaskActions {
    Uni<Task> createOne(CreateTask command);
    Uni<List<Task>> createMany(List<CreateTask> commands);
  }

  interface UpdateTaskActions {
    Uni<Task> updateOne(TaskAction command);
    Uni<Task> updateOne(List<TaskAction> commands);
    Uni<List<Task>> updateMany(List<TaskAction> commands);
  }

  interface DeleteTaskActions {
    Multi<Task> deleteAll();
  }

  interface ActiveTaskActions {
    Multi<Task> findAll();
    Multi<Task> findByRoles(List<String> roles);
    Multi<Task> findByAssignee(List<String> assignees);
    Uni<Task> get(String id);
  }
  
}
