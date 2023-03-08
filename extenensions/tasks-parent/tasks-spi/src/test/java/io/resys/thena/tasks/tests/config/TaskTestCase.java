package io.resys.thena.tasks.tests.config;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.resys.thena.tasks.spi.DocumentStoreImpl;
import io.resys.thena.tasks.spi.TaskClientImpl;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskTestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  private DocumentStoreImpl store;
  private TaskClientImpl client;
  private static final String DB = "junit-tasks-"; 
  private static final AtomicInteger ID = new AtomicInteger();

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

}
