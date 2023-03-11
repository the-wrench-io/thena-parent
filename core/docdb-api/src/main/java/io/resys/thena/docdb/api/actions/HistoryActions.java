package io.resys.thena.docdb.api.actions;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.api.models.ObjectsResult.ObjectsStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.smallrye.mutiny.Uni;

public interface HistoryActions {

  BlobHistoryBuilder blob();
  
  interface BlobHistoryBuilder {
    BlobHistoryBuilder repo(String repo, String headName);
    BlobHistoryBuilder criteria(BlobCriteria ... criteria);
    BlobHistoryBuilder criteria(List<BlobCriteria> criteria);

    BlobHistoryBuilder blobName(String blobName); // entity name
    BlobHistoryBuilder latestOnly(); // search only from last known version
    BlobHistoryBuilder latestOnly(boolean latest); // search only from last known version
    Uni<BlobHistoryResult> build();
  }
  
  @Value.Immutable
  interface BlobHistoryResult {
    List<BlobHistory> getValues();
    
    @Nullable Repo getRepo();    
    ObjectsStatus getStatus();
    List<Message> getMessages();
  }

}
