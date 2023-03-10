package io.resys.thena.tasks.tests;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.api.TaskClient;
import io.resys.thena.tasks.api.actions.ChangeActions.CreateTask;
import io.resys.thena.tasks.api.actions.ImmutableCreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class ParallelTaskMetricTest extends TaskTestCase {
  private AtomicInteger index = new AtomicInteger(0);
  private AtomicInteger fails = new AtomicInteger(0);
  
  @org.junit.jupiter.api.Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName("init-test").create().await().atMost(atMost);

    final var config = getStore().getConfig();
    final var repos = config.getClient().repo().query().find().collect().asList().await().atMost(atMost);
    
    
    try {
      runInserts(client, 1);
      runInserts(client, 50);
    } catch(Exception e) { 
    
      assertCommits("init-test");
      throw e;
    }
    
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
    
    Multi.createFrom().items(bulk.stream()).onItem().transformToUni(item -> {
      
      final var commit = client.changes().create(item)
          
      .onItem().transform(c -> {
        log.debug("Record stored: {}", index.getAndIncrement());
        return c;
      })
      .onFailure().recoverWithItem((error) -> {
        error.printStackTrace();
        log.debug("Record failed: {}, error {}", fails.getAndIncrement(), error.getMessage());
        return null;
      });
      
      return commit;
      /*
      return commit.onFailure(ex -> {
        log.debug("Record stored Failed: {}", index.get());
        return true;
      }).retry().withBackOff(Duration.ofSeconds(3)).atMost(30);
      */
      
    })
    .merge(5)
    .collect().asList()
    .onItem().transformToUni(e -> {
      final var start = System.currentTimeMillis();
      
      return client.query().active().findAll().onItem().transform(blobs -> {
        final var end = System.currentTimeMillis();
        log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
        return Uni.createFrom().nullItem();
      }).onItem().transformToUni(junk -> {
        
        
        final var config = getStore().getConfig();
        return config.getClient().commit().query().head("init-test").findAllCommits()
        .onItem().transformToUni(commits -> {          
          log.debug("Total commits: {}, fails: {}, items: {}", commits.size(), fails.get(), bulk.size());
          return Uni.createFrom().nullItem();
        });
      }).onItem().transformToUni(junk -> {
        
        
        final var config = getStore().getConfig();
        return config.getClient().commit().query().head("init-test").findAllCommitTrees()
        .onItem().transformToUni(trees -> {          
          log.debug("Total Trees: {}", trees.size());
          trees.forEach(tree -> {
            log.debug("Tree values: {}", tree.getValues().size());  
          });
          return Uni.createFrom().nullItem();
        });
      });



      
    })
    .await().atMost(atMost);
    
    
  }
}
