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
import io.smallrye.mutiny.Uni;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("q/tasks/api/init/")
public class InitResource {

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
  public Uni<HeadState> init() {
    return client.repo().query().repoName("tasks-repo").headName("main").createIfNot()
        .onItem().transform(created -> HeadState.builder().created(true).build());
  }
}
