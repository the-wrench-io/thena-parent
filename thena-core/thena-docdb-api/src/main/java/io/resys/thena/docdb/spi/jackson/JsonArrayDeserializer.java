package io.resys.thena.docdb.spi.jackson;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.vertx.core.json.JsonArray;

public class JsonArrayDeserializer extends JsonDeserializer<JsonArray> {
  @Override
  public JsonArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final var text = ctxt.readValue(p, List.class);
    try {
      return new JsonArray(text);
    } catch (IllegalArgumentException e) {
      throw new InvalidFormatException(p, "Expected a json array", text, JsonArray.class);
    }
  }
}