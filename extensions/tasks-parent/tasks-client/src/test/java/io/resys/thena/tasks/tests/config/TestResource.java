package io.resys.thena.tasks.tests.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.resys.thena.tasks.client.api.model.ImmutableProject;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Project;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.TasksRestApi;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class TestResource implements TasksRestApi {

  private final ImmutableTask mockTask = ImmutableTask.builder()
      .id("task1")
      .version("task-version1")
      .created(TaskTestCase.getTargetDate())
      .updated(TaskTestCase.getTargetDate())
      .title("task-title1")
      .priority(Task.Priority.HIGH)
      .status(Task.Status.CREATED)
      .description("Very good task indeed")
      .reporterId("John Smith")
      .build();

  @Override
  public Uni<List<Project>> findProjects() {
    return Uni.createFrom().item(Arrays.asList(ImmutableProject.builder()
        .id("project1")
        .version("project-version1")
        .build()));
  }  
  
  @Override
  public Uni<List<Task>> findTasks(String projectId) {
    return Uni.createFrom().item(Arrays.asList(mockTask));
  }

  @Override
  public Uni<List<Task>> createTasks(String projectId, List<CreateTask> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTask).collect(Collectors.toList()));
  }
  
  @Override
  public Uni<List<Task>> updateTasks(String projectId, List<TaskUpdateCommand> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTask).collect(Collectors.toList()));
  }

  @Override
  public Uni<Task> updateTask(String projectId, String taskId, List<TaskUpdateCommand> commands) {
    return Uni.createFrom().item(mockTask);
  }

}
