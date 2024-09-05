package io.resys.thena.tasks.client.rest;

/*-
 * #%L
 * thena-tasks-client
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

import java.time.LocalDate;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.resys.thena.tasks.client.api.model.Project;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.smallrye.mutiny.Uni;


@Path("q/tasks/api/projects/")
public interface TasksRestApi {
  
  @GET @Path("") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Project>> findProjects();
  
  @GET @Path("{projectId}/tasks") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Task>> findTasks(@PathParam("projectId") String projectId);
 
  @POST @Path("{projectId}/tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> createTasks(@PathParam("projectId") String projectId, List<CreateTask> commands);
  
  @PUT @Path("{projectId}/tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> updateTasks(@PathParam("projectId") String projectId,  List<TaskUpdateCommand> commands);
  
  @DELETE @Path("{projectId}/tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> deleteTasks(@PathParam("projectId") String projectId, List<TaskUpdateCommand> commands);

  @PUT @Path("{projectId}/tasks/{taskId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Task> updateTask(@PathParam("projectId") String projectId, @PathParam("taskId") String taskId, List<TaskUpdateCommand> commands);

  @GET @Path("{projectId}/archive/{fromCreatedOrUpdated}/tasks") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Task>> findArchivedTasks(
      @PathParam("projectId") String projectId, 
      @PathParam("fromCreatedOrUpdated") LocalDate fromCreatedOrUpdated);
  
  @DELETE @Path("{projectId}/tasks/{taskId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Task> deleteOneTask(@PathParam("projectId") String projectId, @PathParam("taskId") String taskId, List<TaskUpdateCommand> command);
  
}
