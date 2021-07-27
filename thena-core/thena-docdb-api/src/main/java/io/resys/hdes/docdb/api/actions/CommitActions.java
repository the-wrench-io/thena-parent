package io.resys.hdes.docdb.api.actions;

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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.hdes.docdb.api.models.Message;
import io.resys.hdes.docdb.api.models.Objects.Commit;
import io.smallrye.mutiny.Uni;

public interface CommitActions {
  HeadCommitBuilder head();
  MergeBuilder merge();
  RebaseBuilder rebase();
  
  interface RebaseBuilder {
    RebaseBuilder author(String author);
    RebaseBuilder message(String message);
    RebaseBuilder id(String headGid); // head GID to what to append
    RebaseBuilder head(String repoId, @Nullable String headName); // head name, if null main is set
    Uni<CommitResult> build();
  }
  
  interface MergeBuilder {
    MergeBuilder repo(String repoId);
    MergeBuilder from(String fromHeadName);
    MergeBuilder to(String fromHeadName);
    MergeBuilder author(String author);
    MergeBuilder message(String message);
    Uni<CommitResult> build();
  }
  
  interface HeadCommitBuilder {
    HeadCommitBuilder id(String headGid); // head GID to what to append
    HeadCommitBuilder parent(String parentCommit); // for validations
    HeadCommitBuilder head(String repoId, String headName); // head GID to what to append
    HeadCommitBuilder append(String name, String blob);
    HeadCommitBuilder remove(String name);
    HeadCommitBuilder author(String author);
    HeadCommitBuilder message(String message);
    Uni<CommitResult> build();
  }
  
  enum CommitStatus {
    OK, ERROR, CONFLICT
  }
  
  @Value.Immutable
  interface CommitResult {
    String getGid(); // repo/head
    @Nullable
    Commit getCommit();
    CommitStatus getStatus();
    List<Message> getMessages();
  }
}
