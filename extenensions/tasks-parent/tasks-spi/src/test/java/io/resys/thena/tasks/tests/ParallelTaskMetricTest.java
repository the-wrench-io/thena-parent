package io.resys.thena.tasks.tests;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.api.TaskClient;
import io.resys.thena.tasks.api.actions.ChangeActions.CreateTask;
import io.resys.thena.tasks.api.actions.ImmutableCreateTask;
import io.resys.thena.tasks.spi.store.MainBranch;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class ParallelTaskMetricTest extends TaskTestCase {
  private AtomicInteger index = new AtomicInteger(0);
  private AtomicInteger fails = new AtomicInteger(0);
  private String repoName = "init-test";
  
  @org.junit.jupiter.api.Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName(repoName).create().await().atMost(atMost);
    
    // first commit
    client.changes().create(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .subject("very important subject no: init")
        .description("first task ever no: init")
        .priority(1)
        .addAssigneeRoles("admin-users", "view-only-users")
        .userId("user-1")
        .build())
    .await().atMost(atMost);;
    
    
    runInserts(client, 49);
    runSelect(client);    
    
    
    // Assert that there is no data loss from parallel processing
    final var config = getStore().getConfig();
    final var blobState = config.getClient().objects().refState()
        .repo(repoName)
        .ref(MainBranch.HEAD_NAME)
        .blobs(true)
        .build().await().atMost(atMost);
    
    JsonArray blobs = new JsonArray(blobState.getObjects().getBlobs().values().stream()
        .map(b -> b.getValue())
        .toList());
    
    Assertions.assertEquals(50, blobs.size());
    // log.debug(blobs.encodePrettily());
    
    JsonArray expected = new JsonArray();
    expected.add(JsonObject.of("subject", "very important subject no: init"));
    for(int index = 0; index < 49; index++) {
      expected.add(JsonObject.of("subject", "very important subject no: " + index));
    }
    JSONAssert.assertEquals(expected.encodePrettily(), blobs.encodePrettily(), false);
  }
  

  private void runSelect(TaskClient client) {
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
    
    Multi.createFrom().items(bulk.stream()).onItem().transformToUni(item -> {
      final var commit = client.changes().create(item)
      .onItem().transform(c -> {
        log.debug("Record stored: {}", index.getAndIncrement());
        return c;
      })
      .onFailure().recoverWithItem((error) -> {
        log.debug("Record failed: {}, error {}", fails.getAndIncrement(), error.getMessage());
        return null;
      });
      return commit;
    })
    .merge(5)
    .collect().asList()
    .onItem().transformToUni(e -> {
      final var start = System.currentTimeMillis();
      return client.query().active().findAll().onItem().transform(blobs -> {
        // log select time
        final var end = System.currentTimeMillis();
        log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
        return Uni.createFrom().nullItem();
      
      }).onItem().transformToUni(junk -> {
        // log commits
        final var config = getStore().getConfig();
        return config.getClient().commit().query().repoName(repoName).findAllCommits()
        .onItem().transformToUni(commits -> {          
          log.debug("Total commits: {}, fails: {}, items: {}", commits.size(), fails.get(), bulk.size());
          return Uni.createFrom().nullItem();
        });
      }).onItem().transformToUni(junk -> {
        // log trees
        final var config = getStore().getConfig();
        return config.getClient().commit().query().repoName(repoName).findAllCommitTrees()
        .onItem().transformToUni(trees -> {          
          log.debug("Total Trees: {}", trees.size());
          return Uni.createFrom().nullItem();
        });
        
      });
    })
    .await().atMost(atMost);
    
    
  }
}
