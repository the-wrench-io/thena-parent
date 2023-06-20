package io.resys.thena.tasks.dev.app;

/*-
 * #%L
 * thena-quarkus-dev-app
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import io.quarkus.vertx.http.Compressed;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("q/tasks/api/")
@Slf4j
public class TaskResource {

  @Inject
  TaskClient client;

  @Compressed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("active/tasks")
  public Uni<List<Task>> findAllActiveTasks() {
    return client.tasks().queryActiveTasks().findAll().collect().asList();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("task/{blobId}")
  public Uni<Task> findTaskByBlobId(@PathParam("blobId") String blobId) {
    return client.tasks().queryActiveTasks().get(blobId);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tasks")
  public Multi<Task> findTasksByIds(List<String> taskIds) {
    return client.tasks().queryActiveTasks().findByTaskIds(taskIds);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("task")
  public Uni<Task> createTask(TaskCommand.CreateTask command) {
    return client.tasks().createTask().createOne(command);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tasks")
  public Uni<List<Task>> createTasks(List<TaskCommand.CreateTask> commands) {
    return client.tasks().createTask().createMany(commands);
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("task/{taskId}")
  public Uni<Task> updateTask(@PathParam("taskId") String taskId, TaskCommand.TaskUpdateCommand command) {
    return client.tasks().updateTask().updateOne(command);
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tasks")
  public Uni<List<Task>> updateTasks(List<TaskCommand.TaskUpdateCommand> commands) {
    return client.tasks().updateTask().updateMany(commands);
  }
}
