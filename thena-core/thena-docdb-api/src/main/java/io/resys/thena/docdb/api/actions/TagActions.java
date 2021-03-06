package io.resys.thena.docdb.api.actions;

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
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface TagActions {

  TagBuilder create();
  TagQuery query();
  
  interface TagQuery {
    TagQuery repo(String repoId);
    TagQuery tagName(String tagName);
    
    Multi<Tag> find();
    Uni<Optional<Tag>> get();
    Uni<Optional<Tag>> delete();
  }
  
  interface TagBuilder {
    TagBuilder tagName(String name);
    TagBuilder repo(String repoIdOrName, String commitIdOrHead);
    TagBuilder author(String author);
    TagBuilder message(String message);    
    Uni<TagResult> build();
  }

  enum TagStatus {
    OK, ERROR
  }

  @Value.Immutable
  interface TagResult {
    @Nullable
    Tag getTag();
    TagStatus getStatus();
    List<Message> getMessages();
  }
}
