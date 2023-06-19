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


import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.smallrye.mutiny.Uni;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Path("q/tasks/api/demo/")
@Slf4j
public class DemoResource {

  @Inject
  TaskClient client;

  @Jacksonized
  @Data
  @Builder
  public static class HeadState {
    private Boolean created;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("populate/{totalTasks}")
  public Uni<HeadState> populate(@PathParam("totalTasks") String totalTasks) {
    final int count = totalTasks == null ? 50 : Integer.parseInt(totalTasks);

    final var provider =  new RandomDataProvider();
    final var bulk = new ArrayList<TaskCommand.CreateTask>();
    final var targetDate = LocalDateTime.now();

    for(int index = 0; index < count; index++) {
      final var newTask = ImmutableCreateTask.builder()
          .targetDate(targetDate)
          .title(provider.getTitle())
          .description(provider.getDescription())
          .priority(provider.getPriority())
          .roles(provider.getRoles())
          .assigneeIds(provider.getAssigneeIds())
          .reporterId(provider.getReporterId())
          .status(provider.getStatus())
          .userId("demo-gen-1")
          .addAllExtensions(provider.getExtensions())
          .comments(provider.getComments())
          .build();
      bulk.add(newTask);
    }

    return client.tasks().createTask().createMany(bulk).onItem().transform(created -> {
      log.info("Created {} tasks", created.size());
      return HeadState.builder().created(true).build();
    });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("clear")
  public Uni<HeadState> clear() {
    return client.repo().query().repoName("tasks-repo").headName("main").createIfNot()
        .onItem().transformToUni(created -> {

          return client.tasks().queryActiveTasks().deleteAll().collect().asList()
              .onItem().transform(tasks -> HeadState.builder().created(true).build());

        });
  }

}
