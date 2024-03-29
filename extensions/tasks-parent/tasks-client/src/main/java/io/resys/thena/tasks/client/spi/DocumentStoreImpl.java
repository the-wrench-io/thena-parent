package io.resys.thena.tasks.client.spi;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.spi.OidUtils;
import io.resys.thena.docdb.spi.pgsql.DocDBFactoryPgSql;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocumentAuthorProvider;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocumentGidProvider;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentConfig;
import io.resys.thena.tasks.client.spi.store.ImmutableDocumentExceptionMsg;
import io.resys.thena.tasks.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
public class DocumentStoreImpl implements DocumentStore {
  private final DocumentConfig config;
  
  @Override public DocumentConfig getConfig() { return config; }
  @Override public RepositoryQuery repo() {
    return new RepositoryQuery() {
      private String repoName, headName;
      @Override public RepositoryQuery repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public RepositoryQuery headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<DocumentStore> create() { return createRepo(repoName, headName); }
      @Override public DocumentStore build() { return createClientStore(repoName, headName); }
      @Override public Uni<Boolean> createIfNot() { return createRepoOrGetRepo(); }
    };
  }
  
  private Uni<Boolean> createRepoOrGetRepo() {
    final var client = config.getClient();
    
    return client.repo().query().id(config.getRepoName()).get().onItem().transformToUni(repo -> {
      if(repo == null) {
        return client.repo().create().name(config.getRepoName()).build().onItem().transform(newRepo -> true); 
      }
      return Uni.createFrom().item(true);
    });
  }
  
  private Uni<DocumentStore> createRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var newRepo = client.repo().create().name(repoName).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != RepoStatus.OK) {
        throw new DocumentStoreException("REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName, headName);
    });
  }
  
  private DocumentStore createClientStore(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new DocumentStoreImpl(ImmutableDocumentConfig.builder()
        .from(config)
        .repoName(repoName)
        .headName(headName == null ? config.getHeadName() : headName)
        .build());
    
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Slf4j
  @Accessors(fluent = true, chain = true)
  @Getter(AccessLevel.NONE)
  @Data
  public static class Builder {
    private String repoName;
    private String headName;
    private ObjectMapper objectMapper;
    private DocumentGidProvider gidProvider;
    private DocumentAuthorProvider authorProvider;
    private io.vertx.mutiny.pgclient.PgPool pgPool;
    private String pgHost;
    private String pgDb;
    private Integer pgPort;
    private String pgUser;
    private String pgPass;
    private Integer pgPoolSize;
    
    private DocumentGidProvider getGidProvider() {
      return this.gidProvider != null ? this.gidProvider : new DocumentGidProvider() {
        @Override public String getNextVersion(DocumentType entity) { return OidUtils.gen(); }
        @Override public String getNextId(DocumentType entity) { return OidUtils.gen(); }
      };
    }
    
    private DocumentAuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public DocumentStoreImpl build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    
      final var headName = this.headName == null ? MainBranch.HEAD_NAME: this.headName;
      if(log.isDebugEnabled()) {
        final var msg = new StringBuilder()
          .append(System.lineSeparator())
          .append("Configuring Thena: ").append(System.lineSeparator())
          .append("  repoName: '").append(this.repoName).append("'").append(System.lineSeparator())
          .append("  headName: '").append(headName).append("'").append(System.lineSeparator())
          .append("  objectMapper: '").append(this.objectMapper == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  gidProvider: '").append(this.gidProvider == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  authorProvider: '").append(this.authorProvider == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          
          .append("  pgPool: '").append(this.pgPool == null ? "configuring" : "provided").append("'").append(System.lineSeparator())
          .append("  pgPoolSize: '").append(this.pgPoolSize).append("'").append(System.lineSeparator())
          .append("  pgHost: '").append(this.pgHost).append("'").append(System.lineSeparator())
          .append("  pgPort: '").append(this.pgPort).append("'").append(System.lineSeparator())
          .append("  pgDb: '").append(this.pgDb).append("'").append(System.lineSeparator())
          .append("  pgUser: '").append(this.pgUser == null ? "null" : "***").append("'").append(System.lineSeparator())
          .append("  pgPass: '").append(this.pgPass == null ? "null" : "***").append("'").append(System.lineSeparator());
          
        log.debug(msg.toString());
      }
      
      final DocDB thena;
      if(pgPool == null) {
        RepoAssert.notNull(pgHost, () -> "pgHost must be defined!");
        RepoAssert.notNull(pgPort, () -> "pgPort must be defined!");
        RepoAssert.notNull(pgDb, () -> "pgDb must be defined!");
        RepoAssert.notNull(pgUser, () -> "pgUser must be defined!");
        RepoAssert.notNull(pgPass, () -> "pgPass must be defined!");
        RepoAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
        final PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(pgHost)
            .setPort(pgPort)
            .setDatabase(pgDb)
            .setUser(pgUser)
            .setPassword(pgPass);
        final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(pgPoolSize);
        
        final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(connectOptions, poolOptions);
        
        thena = DocDBFactoryPgSql.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      } else {
        thena = DocDBFactoryPgSql.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
      }
      
      final DocumentConfig config = ImmutableDocumentConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gid(getGidProvider())
          .author(getAuthorProvider())
          .build();
      return new DocumentStoreImpl(config);
    }
  }
}
