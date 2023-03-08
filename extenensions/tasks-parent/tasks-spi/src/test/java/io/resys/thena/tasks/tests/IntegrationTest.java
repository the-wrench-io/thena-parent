package io.resys.thena.tasks.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.api.actions.ImmutableCreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class IntegrationTest extends TaskTestCase {
  private final Duration atMost = Duration.ofSeconds(2);
  
  @Test
  public void init() throws JsonProcessingException {
    final var client = getClient().repo().repoName("init-test").create().await().atMost(atMost);
    
    final var task = client.changes().create(ImmutableCreateTask.builder()
        .subject("very important subject")
        .description("first task ever")
        .priority(1)
        .addAssigneeRoles("admin-users", "view-only-users")
        .userId("user-1")
        .build()).await().atMost(atMost);
  }
}
