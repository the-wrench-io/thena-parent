package io.resys.thena.tasks.client.api.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableProject.class) @JsonDeserialize(as = ImmutableProject.class)
public interface Project extends Document {

  @Value.Default
  default DocumentType getDocumentType() {
    return DocumentType.PROJECT;
  }
  
}
