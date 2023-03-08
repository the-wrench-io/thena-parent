package io.resys.thena.tasks.spi.store;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.tasks.spi.store.DocumentStore.DocumentType;


@Value.Immutable
public interface DocumentConfig {
  DocDB getClient();
  String getRepoName();
  String getHeadName();
  DocumentGidProvider getGid();
  DocumentAuthorProvider getAuthor();


  interface DocumentGidProvider {
    String getNextId(DocumentType entity);
    String getNextVersion(DocumentType entity);
  }
  
  @FunctionalInterface
  interface DocumentAuthorProvider {
    String get();
  }
}