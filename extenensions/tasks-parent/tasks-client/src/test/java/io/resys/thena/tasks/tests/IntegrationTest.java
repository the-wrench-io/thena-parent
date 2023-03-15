package io.resys.thena.tasks.tests;

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

import java.time.Duration;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class IntegrationTest extends TaskTestCase {
  private final Duration atMost = Duration.ofSeconds(2);
  
  @Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName("integration-test").create().await().atMost(atMost);
    
    client.changes().create(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .subject("very important subject")
        .description("first task ever")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .build()).await().atMost(atMost);
    
    final var allActive = client.query().active().findAll().collect().asList().await().atMost(atMost);
    Assertions.assertEquals(1, allActive.size());
    
    final var created = JsonObject.mapFrom(allActive.get(0))
        .put("id", "")
        .put("version", "");
    final var actual = created.encode();
    log.debug(actual);
    JSONAssert.assertEquals(
        "{\"documentType\":\"TASK\",\"id\":\"\",\"version\":\"\",\"created\":[2023,1,1,1,1],\"updated\":null,\"actions\":[],\"roles\":[\"admin-users\",\"view-only-users\"],\"owners\":[],\"dueDate\":null,\"subject\":\"very important subject\",\"description\":\"first task ever\",\"priority\":\"LOW\",\"labels\":[],\"extensions\":[],\"externalComments\":[],\"internalComments\":[]}"
        , actual, true);
  }
}
