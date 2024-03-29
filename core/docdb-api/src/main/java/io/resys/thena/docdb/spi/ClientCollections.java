package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Repo;

@Value.Immutable
public abstract class ClientCollections {
  public interface WithOptions<T extends WithOptions<T>> {
    T withOptions(ClientCollections options);
  }
  
  
  public abstract String getDb();
  public abstract String getRepos();
  public abstract String getRefs();
  public abstract String getTags();
  public abstract String getBlobs();
  public abstract String getTrees();
  public abstract String getTreeItems();
  public abstract String getCommits();
  
  public ClientCollections toRepo(Repo repo) {
    String prefix = repo.getPrefix();
    return ImmutableClientCollections.builder()
        .db(this.getDb())
        .repos(this.getRepos())
        .refs(      prefix + this.getRefs())
        .tags(      prefix + this.getTags())
        .blobs(     prefix + this.getBlobs())
        .trees(     prefix + this.getTrees())
        .treeItems( prefix + this.getTreeItems())
        .commits(   prefix + this.getCommits())
        .build();
  }
  
  public static ClientCollections defaults(String db) {
    return ImmutableClientCollections.builder()
        .db(db == null ? "docdb" : db)
        .repos("repos")
        .refs("refs")
        .tags("tags")
        .blobs("blobs")
        .trees("trees")
        .treeItems("treeItems")
        .commits("commits")
        .build();
  }
}
