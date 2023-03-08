package io.resys.thena.tasks.tests;

import java.time.Duration;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.api.actions.ImmutableCreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskMetricTest extends TaskTestCase {
  private final Duration atMost = Duration.ofSeconds(2);
  
  @Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName("init-test").create().await().atMost(atMost);
    
    client.changes().create(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .subject("very important subject")
        .description("first task ever")
        .priority(1)
        .addAssigneeRoles("admin-users", "view-only-users")
        .userId("user-1")
        .build()).await().atMost(atMost);
    
    final var allActive = client.query().active().findAll().await().atMost(atMost);
    Assertions.assertEquals(1, allActive.size());
    
    final var created = JsonObject.mapFrom(allActive.get(0))
        .put("id", "")
        .put("version", "");
    final var actual = created.encode();
    log.debug(actual);
    JSONAssert.assertEquals("{\"documentType\":\"TASK\",\"id\":\"\",\"version\":\"\",\"created\":{\"dateTime\":[2023,1,1,1,1],\"userId\":\"user-1\"},\"completed\":null,\"updated\":null,\"assigneeRoles\":[\"admin-users\",\"view-only-users\"],\"assigneeId\":null,\"dueDate\":null,\"subject\":\"very important subject\",\"description\":\"first task ever\",\"priority\":1,\"labels\":[],\"extensions\":[],\"externalComments\":[],\"internalComments\":[]}", actual, true);
  }
}
