package io.resys.thena.docdb.test;

import java.time.Duration;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
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
    super((client, repo) -> {
      
      client.commit().head()
          .head(repo.getName(), "main")
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
      .head(getRepo().getName(), "main")
      .append(id, JsonObject.of(
        "type", "person",
        "name", "sam", "lastName", "vimes",
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
  
  public void addCassandraChaseChanges(int changes, String id) {
    final var client = getClient();
    for(int index = 0; index < changes; index++) { 
      client.commit().head()
      .head(getRepo().getName(), "main")
      .append(id, JsonObject.of(
        "type", "person",
        "name", "cassandra", "lastName", "chase",
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
  public void findByExactMatchFromAllPossibleCases() throws JSONException {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    final var history = getClient().history().blob()
      .repo(getRepo().getName(), "main")
      .criteria(ImmutableBlobCriteria.builder().type(CriteriaType.EXACT).key("name").value("sam").build())
      .latestOnly()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("Found type=person from history, body: {}", JsonArray.of(history).toString());
    Assertions.assertEquals(1, history.getValues().size());
    
    final var first = history.getValues().get(0);
    Assertions.assertEquals("ID-1", first.getTreeValueName());
    Assertions.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode());
  }
  
  @Test
  public void findByLikeMatchFromAllPossibleCases() {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    final var history = getClient().history().blob()
      .repo(getRepo().getName(), "main")
      .criteria(ImmutableBlobCriteria.builder().type(CriteriaType.LIKE).key("name").value("sam").build())
      .latestOnly()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("Found type=person from history, body: {}", JsonArray.of(history).toString());
    Assertions.assertEquals(1, history.getValues().size());
    
    final var first = history.getValues().get(0);
    Assertions.assertEquals("ID-1", first.getTreeValueName());
    Assertions.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode());
  }
 
  
  @Test
  public void findByExactMatchAllCommits() {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    
    var history = getClient().history().blob()
      .repo(getRepo().getName(), "main")
      .criteria(ImmutableBlobCriteria.builder().type(CriteriaType.LIKE).key("name").value("sam").build())
      .latestOnly(false)
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(41, history.getValues().size());
    
    history = getClient().history().blob()
        .repo(getRepo().getName(), "main")
        .criteria(ImmutableBlobCriteria.builder().type(CriteriaType.EXACT).key("name").value("sam").build())
        .latestOnly(false)
        .build()
        .await().atMost(Duration.ofMinutes(1));
      
    Assertions.assertEquals(41, history.getValues().size());
  }
}
