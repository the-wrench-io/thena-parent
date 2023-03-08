package io.resys.thena.tasks.dev.app;

import javax.inject.Inject;
import javax.ws.rs.Path;

import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Path("portal")
@Slf4j
public class TestResource {

  @Inject Vertx vertx;
  
}
