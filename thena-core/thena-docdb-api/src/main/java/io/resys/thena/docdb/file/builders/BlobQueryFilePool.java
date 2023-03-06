package io.resys.thena.docdb.file.builders;

/*-
 * #%L
 * thena-docdb-pgsql
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.BlobCriteria;
import io.resys.thena.docdb.spi.ClientQuery.BlobQuery;
import io.resys.thena.docdb.spi.ClientQuery.CriteriaType;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlobQueryFilePool implements BlobQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;
  private final List<BlobCriteria> blobCriteria = new ArrayList<>();

  @Override
  public Uni<Blob> getById(String blobId) {
    final var sql = builder.blobs().getById(blobId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transform((Collection<Blob> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by 'id': '" + blobId + "'!", e));
  }
  @Override
  public Uni<List<Blob>> findById(List<String> blobId) {
    final var sql = builder.blobs().findByIds(blobId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transform((Collection<Blob> rowset) -> {
          List<Blob> result = new ArrayList<Blob>();
          for(final var item : rowset) {
            if(isMatch(item, blobCriteria)) {
              result.add(item);
            }
          }
          return result;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by 'id'-s: '" + String.join(",", blobId) + "'!", e));
  }
  @Override
  public Multi<Blob> find() {
    final var sql = builder.blobs().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB'!", e));
  }
  @Override
  public Multi<Blob> findByTreeId(String treeId) {
    final var sql = builder.blobs().findByTreeId(treeId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transform((Collection<Blob> rowset) -> {
          List<Blob> result = new ArrayList<Blob>();
          for(final var item : rowset) {
            if(isMatch(item, blobCriteria)) {
              result.add(item);
            }
          }
          return result;
        })
        .onItem()
        .transformToMulti((List<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by tree: " + treeId + "!", e));
  }
  @Override
  public BlobQuery criteria(BlobCriteria... criteria) {
    this.blobCriteria.addAll(Arrays.asList(criteria));
    return this;
  }
  @Override
  public BlobQuery criteria(List<BlobCriteria> criteria) {
    this.blobCriteria.addAll(criteria);
    return this;
  }
  
  public static boolean isMatch(Blob item, List<BlobCriteria> blobCriteria) {
    var found = true;
    for(final var crit : blobCriteria) {
      final var field = crit.getKey();
      if(!item.getValue().containsKey(field)) {
        found = false;
        break;
      }
      final var jsonValue = item.getValue().getValue(field);
      if(jsonValue == null) {
        found = false;
        break;
      }
      if(crit.getType() == CriteriaType.EXACT && jsonValue.toString().equals(crit.getValue())) {
        continue;
      }
      if(crit.getType() == CriteriaType.LIKE && jsonValue.toString().indexOf(crit.getValue()) > -1) {
        continue;
      }
      found = false;
      break;
    }
    return found;
  }
}
