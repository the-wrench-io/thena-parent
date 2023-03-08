package io.resys.thena.tasks.api.model;

import java.io.Serializable;

public interface Document extends Serializable {
  DocumentType getDocumentType();
  String getId();
  String getVersion();
  
  enum DocumentType { PROJECT, TASK }
  
}
