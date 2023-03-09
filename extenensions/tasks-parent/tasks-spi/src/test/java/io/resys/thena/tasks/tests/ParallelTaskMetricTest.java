package io.resys.thena.tasks.tests;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.api.TaskClient;
import io.resys.thena.tasks.api.actions.ChangeActions.CreateTask;
import io.resys.thena.tasks.api.actions.ImmutableCreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class ParallelTaskMetricTest extends TaskTestCase {
  private final Duration atMost = Duration.ofMinutes(5);
  
  @org.junit.jupiter.api.Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName("init-test").create().await().atMost(atMost);
    
    runInserts(client, 1000);
    select(client);
  }
  
  
  private void select(TaskClient client) {
    final var start = System.currentTimeMillis();
    
    final var blobs = client.query().active().findAll().await().atMost(Duration.ofMinutes(1));
    final var end = System.currentTimeMillis();
    
    log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
  }
  
  
  
  private void runInserts(TaskClient client, int total) {
    
    List<CreateTask> bulk = new ArrayList<>();
    for(int index = 0; index < total; index++) {
      final var newTask = ImmutableCreateTask.builder()
      .targetDate(getTargetDate())
      .subject("very important subject no: " + index)
      .description("first task ever no: "  + index)
      .priority(1)
      .addAssigneeRoles("admin-users", "view-only-users")
      .userId("user-1")
      .build();
      bulk.add(newTask);
    }
    
    bulk.stream().parallel().forEach(item -> client.changes().create(item).await().atMost(atMost));
    
    
  }
}
