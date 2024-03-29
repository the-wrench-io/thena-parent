package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-api
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
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ImmutableBlobCriteria;
import io.resys.thena.docdb.spi.pgsql.config.PgDbTestTemplate;
import io.resys.thena.docdb.spi.pgsql.config.PgProfile;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@QuarkusTest
@TestProfile(PgProfile.class)
public class SearchBlobFromHistoryPGDBTest extends PgDbTestTemplate {

  
  public SearchBlobFromHistoryPGDBTest() {
    super((client, repo) -> {
      
      client.commit().builder()
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
      client.commit().builder()
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
      client.commit().builder()
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
    JSONAssert.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode(), false);
  }
  
  @Test
  public void findByLikeMatchFromAllPossibleCases() throws JSONException {
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
    JSONAssert.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode(), false);
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
