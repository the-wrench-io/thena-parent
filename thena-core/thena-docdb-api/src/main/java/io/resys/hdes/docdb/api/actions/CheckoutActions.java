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

import java.time.LocalDateTime;

import org.immutables.value.Value;

import io.smallrye.mutiny.Multi;

public interface CheckoutActions {
  CommitCheckout commit();
  TagCheckout tag();
  HeadCheckout head();
  
  interface CommitCheckout {
    CommitCheckout repo(String repoId, String commitId);
    CommitCheckout gid(String commitGid);
    Multi<CheckoutResult> build();
  }
  
  interface HeadCheckout {
    HeadCheckout repo(String repoId, String headName);
    HeadCheckout gid(String headGid);
    Multi<CheckoutResult> build();
  }
  
  interface TagCheckout {
    TagCheckout repo(String repoId, String tagName);
    TagCheckout gid(String tagGid);
    Multi<CheckoutResult> build();
  }
  
  @Value.Immutable
  interface CheckoutResult {
    String getGid();
    String getRepo();
    String getCommit();
    LocalDateTime getModified();
    String getName();
    String getValue();
  }
}
