package io.resys.thena.docdb.test;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@QuarkusTest
@TestProfile(PgProfile.class)
public class SearchBlobFromHistoryTest extends DbTestTemplate {

  
  public SearchBlobFromHistoryTest() {
    super((client) -> {
      final var repo = client.repo().create()
          .name(SearchBlobFromHistoryTest.class.getSimpleName())
          .build()
          .await().atMost(Duration.ofMinutes(1));
      
      client.commit().head()
          .head(repo.getRepo().getName(), "main")
          .append("ID-1", new JsonObject(Map.of(
              "type", "person",
              "name", "sam", 
              "lastName", "vimes")))
          
          .append("ID-2", new JsonObject(Map.of(
              "type", "person",
              "name", "cassandra", 
              "lastName", "chase")))
          
          .append("ID-3", new JsonObject(Map.of(
              "type", "person",
              "name", "count", 
              "lastName", "sober")))
          
          .author("tester bob")
          .message("first commit!")
          .build()
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
          .await().atMost(Duration.ofMinutes(1));
      
    });
  }
  
  public void addSamVimesChanges(int changes, String id) {
    final var client = getClient();
    for(int index = 0; index < changes; index++) { 
      client.commit().head()
      .head(SearchBlobFromHistoryTest.class.getSimpleName(), "main")
      .append(id, JsonObject.of(
        "type", "person",
        "name", "same", "lastName", "vimes",
        "change id", (index+1) + " of changes: " + changes 
        ))
      .author("tester bob")
      .message("change commit!")
      .parentIsLatest()
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    }
  }


  @Test
  public void crateRepoAddAndDeleteFile() {
    //final var repo = getClient().repo().query().id(SearchBlobFromHistoryTest.class.getSimpleName()).get().await().atMost(Duration.ofMinutes(1));
    //super.printRepo(repo);
    addSamVimesChanges(20, "ID-1");
    
    final var history = getClient().history().blob()
      .repo(SearchBlobFromHistoryTest.class.getSimpleName(), "main")
      .entry("name", "sam")
      .latestOnly()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("Found type=person from history, body: {}", JsonArray.of(history).toString());
  }
  
}
