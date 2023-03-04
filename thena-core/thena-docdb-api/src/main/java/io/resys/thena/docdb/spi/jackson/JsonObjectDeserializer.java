package io.resys.thena.docdb.spi.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.vertx.core.json.JsonObject;

public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {

  @SuppressWarnings("unchecked")
  @Override
  public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final var text = ctxt.readValue(p, Map.class);
    try {
      return new JsonObject(text);
    } catch (IllegalArgumentException e) {
      throw new InvalidFormatException(p, "Expected a json object", text, JsonObject.class);
    }
  }
}
