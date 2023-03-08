package io.resys.thena.tasks.spi.store;

import io.smallrye.mutiny.Uni;

public interface DocumentStore {
  RepositoryQuery repo();
  DocumentConfig getConfig();
  
  enum DocumentType { PROJECT, TASK }
  
  interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    RepositoryQuery headName(String headName);
    Uni<DocumentStore> create();    
    DocumentStore build();
    Uni<Boolean> createIfNot();
  } 
  

}