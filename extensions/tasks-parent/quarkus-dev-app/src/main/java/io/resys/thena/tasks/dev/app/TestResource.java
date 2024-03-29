package io.resys.thena.tasks.dev.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.vertx.http.Compressed;
import io.resys.thena.tasks.client.api.TasksClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Path("q/tasks/api/")
@Slf4j
public class TestResource {
  @Inject Vertx vertx;
  @Inject TasksClient client;

  //http://localhost:8080/portal/active/tasks
  @Jacksonized @Data @Builder
  public static class HeadState {
    private Boolean created;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("demo/populate/{totalTasks}")
  public Uni<HeadState> populate(@PathParam("totalTasks") String totalTasks) {
    final int count = totalTasks == null ? 50 : Integer.parseInt(totalTasks);

    
    final var provider =  new RandomDataProvider();
    final var bulk = new ArrayList<CreateTask>();
    final var targetDate = LocalDateTime.now();
    
    for(int index = 0; index < count; index++) {
      final var newTask = ImmutableCreateTask.builder()
      .targetDate(targetDate)
      .subject(provider.getSubject())
      .description(provider.getDescription())
      .priority(provider.getPriority())
      .roles(provider.getRoles())
      .owners(provider.getOwners())
      .status(provider.getStatus())
      .userId("demo-gen-1")
      .addAllExtensions(provider.getExtensions())
      .addAllExternalComments(provider.getExtComments())
      .addAllInternalComments(provider.getIntComments())
      .build();
      bulk.add(newTask);
    }
    
    return client.repo().createIfNot()
        .onItem().transformToUni(created -> {
          if(created) {
            return client.changes().create(bulk)
                .onItem().transform(tasks -> HeadState.builder().created(created).build());
          }
          return Uni.createFrom().item(HeadState.builder().created(created).build());
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("demo/clear")
  public Uni<HeadState> clear() {
    return client.repo().createIfNot()
        .onItem().transformToUni(created -> {
          if(created) {
            return client.query().delete().deleteAll().collect().asList()
                .onItem().transform(tasks -> HeadState.builder().created(created).build());
          }
          return Uni.createFrom().item(HeadState.builder().created(created).build());
        });
  }
  
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("init")
  public Uni<HeadState> init() {
    return client.repo().createIfNot()
        .onItem().transform(created -> HeadState.builder().created(created).build());
  }
  
  @Compressed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("active/tasks")
  public Uni<List<Task>> findAllActiveTasks() {
    return client.query().active().findAll().collect().asList();
  }
}
