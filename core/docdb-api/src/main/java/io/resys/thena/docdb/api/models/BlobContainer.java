package io.resys.thena.docdb.api.models;

import java.util.List;

import io.vertx.core.json.JsonObject;

public interface BlobContainer {
  <T> List<T> accept(BlobVisitor<T> visitor);

  @FunctionalInterface
  interface BlobVisitor<T> {
    T visit(JsonObject blobValue);
  }

}
