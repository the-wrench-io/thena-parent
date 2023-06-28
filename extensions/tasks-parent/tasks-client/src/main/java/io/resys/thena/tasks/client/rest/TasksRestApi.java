package io.resys.thena.tasks.client.rest;

import java.time.LocalDate;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
