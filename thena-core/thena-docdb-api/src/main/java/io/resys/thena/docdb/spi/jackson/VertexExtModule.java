package io.resys.thena.docdb.spi.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VertexExtModule extends SimpleModule {

  private static final long serialVersionUID = 2531090383960153337L;

  public VertexExtModule() {
    // custom types
    addDeserializer(JsonObject.class, new JsonObjectDeserializer());
    addDeserializer(JsonArray.class, new JsonArrayDeserializer());
  }
}