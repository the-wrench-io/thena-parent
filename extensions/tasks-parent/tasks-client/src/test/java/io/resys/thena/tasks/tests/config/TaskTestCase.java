package io.resys.thena.tasks.tests.config;

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
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.resys.thena.tasks.client.spi.DocumentStoreImpl;
import io.resys.thena.tasks.client.spi.TaskClientImpl;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskTestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  public final Duration atMost = Duration.ofMinutes(5);
  
  private DocumentStoreImpl store;
  private TaskClientImpl client;
  private static final String DB = "junit-tasks-"; 
  private static final AtomicInteger ID = new AtomicInteger();
  private static final LocalDateTime targetDate = LocalDateTime.of(2023, 01, 01, 1, 1);
  
  @BeforeEach
  public void setUp() {
    store = DocumentStoreImpl.builder().repoName(DB).pgPool(pgPool).pgDb(DB + ID.getAndIncrement()).build();
    client = new TaskClientImpl(store);
    
    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
      };
    DatabindCodec.mapper().registerModules(modules);
    DatabindCodec.prettyMapper().registerModules(modules);    
  }

  public void assertCommits(String repoName) {
    final var config = getStore().getConfig();
    final var state = ((DocDBDefault) config.getClient()).getState();
    final var commits = config.getClient().commit().query().repoName(repoName).findAllCommits().await().atMost(atMost);
    log.debug("Total commits: {}", commits.size());
    
  }
  
  @AfterEach
  public void tearDown() {
    store = null;
  }

  public DocumentStoreImpl getStore() {
    return store;
  }

  public TaskClientImpl getClient() {
    return client;
  }

  public static LocalDateTime getTargetDate() {
    return targetDate;
  }

}
