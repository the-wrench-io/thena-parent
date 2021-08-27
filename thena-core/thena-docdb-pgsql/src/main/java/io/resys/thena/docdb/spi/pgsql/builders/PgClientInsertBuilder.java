package io.resys.thena.docdb.spi.pgsql.builders;

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

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ImmutableInsertResult;
import io.resys.thena.docdb.spi.ImmutableUpsertResult;
import io.resys.thena.docdb.spi.pgsql.sql.PgErrors;
import io.resys.thena.docdb.spi.pgsql.support.ClientWrapper;
import io.resys.thena.docdb.spi.sql.SqlBuilder;
import io.resys.thena.docdb.spi.sql.SqlMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.SqlClientHelper;

public class PgClientInsertBuilder implements ClientInsertBuilder {
  private final PgPool client;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;

  public PgClientInsertBuilder(ClientWrapper wrapper, SqlMapper sqlMapper, SqlBuilder sqlBuilder) {
    super();
    this.client = wrapper.getClient();
    this.sqlMapper = sqlMapper;
    this.sqlBuilder = sqlBuilder;
  }
  

  @Override
  public Uni<InsertResult> tag(Tag tag) {
    final var tagInsert = sqlBuilder.tags().insertOne(tag);
    return client.preparedQuery(tagInsert.getValue()).execute(tagInsert.getProps())
        .onItem().transform(inserted -> (InsertResult) ImmutableInsertResult.builder().duplicate(false).build())
        .onFailure(e -> PgErrors.duplicate(e))
        .recoverWithItem(e -> ImmutableInsertResult.builder().duplicate(true).build())
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't insert into 'TAG': '" + tagInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> blob(Blob blob) {
    final var blobsInsert = sqlBuilder.blobs().insertOne(blob);
    return client.preparedQuery(blobsInsert.getValue()).execute(blobsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(blob.getId())
            .isModified(true)
            .target(blob)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Blob with id:")
                    .append(" '").append(blob.getId()).append("'")
                    .append(" has been saved.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> PgErrors.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(blob.getId())
            .isModified(false)
            .target(blob)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Blob with id:")
                    .append(" '").append(blob.getId()).append("'")
                    .append(" is already saved.")
                    .toString())
                .build())
            .build())
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't insert into 'BLOB': '" + blobsInsert.getValue() + "'!", e));
  }

  public Uni<UpsertResult> ref(Ref ref, Commit commit) {
    final var findByName = sqlBuilder.refs().getByName(ref.getName());
    return client.preparedQuery(findByName.getValue())
        .mapping(r -> sqlMapper.ref(r))
        .execute(findByName.getProps())

    .onItem().transformToUni(item -> {
      final var exists = item.iterator();
      if(!exists.hasNext()) {
        return createRef(ref, commit);
      }
      return updateRef(exists.next(), commit);
    });
  }
  
  
  
  public Uni<UpsertResult> updateRef(Ref ref, Commit commit) {
    final var refInsert = sqlBuilder.refs().updateOne(ref, commit);
    return client.preparedQuery(refInsert.getValue()).execute(refInsert.getProps())
        .onItem()
        .transform(updateResult -> {
          if(updateResult.size() == 1) {
            return (UpsertResult) ImmutableUpsertResult.builder()
                .id(ref.getName())
                .isModified(true)
                .status(UpsertStatus.OK)
                .target(ref)
                .message(ImmutableMessage.builder()
                    .text(new StringBuilder()
                        .append("Ref with id:")
                        .append(" '").append(ref.getName()).append("'")
                        .append(" has been updated.")
                        .toString())
                    .build())
                .build();
          }
          return (UpsertResult) ImmutableUpsertResult.builder()
              .id(ref.getName())
              .isModified(false)
              .status(UpsertStatus.CONFLICT)
              .target(ref)
              .message(ImmutableMessage.builder()
                  .text(new StringBuilder()
                      .append("Ref with")
                      .append(" id: '").append(ref.getName()).append("',")
                      .append(" commit: '").append(ref.getCommit()).append("'")
                      .append(" is behind of the head.")
                      .toString())
                  .build())
              .build();
        });
  }
  
  
  private Uni<UpsertResult> createRef(Ref ref, Commit commit) {
    final var refsInsert = sqlBuilder.refs().insertOne(ref);
    return client.preparedQuery(refsInsert.getValue()).execute(refsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(ref.getName())
            .isModified(true)
            .target(ref)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Ref with id:")
                    .append(" '").append(ref.getName()).append("'")
                    .append(" has been created.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> PgErrors.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
          .id(ref.getName())
          .isModified(false)
          .target(ref)
          .status(UpsertStatus.CONFLICT)
          .message(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Ref with id:")
                  .append(" '").append(ref.getName()).append("'")
                  .append(" is already created.")
                  .toString())
              .build())
          .build())
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't insert into 'REF': '" + refsInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> tree(Tree tree) {
    final var treeInsert = sqlBuilder.trees().insertOne(tree);
    final var treeValueInsert = sqlBuilder.treeItems().insertAll(tree);
    return SqlClientHelper.inTransactionUni(client, tx -> {
      return tx.preparedQuery(treeInsert.getValue()).execute(treeInsert.getProps())
          .onItem().transformToUni(junk -> 
            tx.preparedQuery(treeValueInsert.getValue()).executeBatch(treeValueInsert.getProps()));
    }).onItem()
    .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
        .id(tree.getId())
        .isModified(true)
        .target(tree)
        .status(UpsertStatus.OK)
        .message(ImmutableMessage.builder()
            .text(new StringBuilder()
                .append("Tree with id:")
                .append(" '").append(tree.getId()).append("'")
                .append(" has been saved.")
                .toString())
            .build())
        .build()
    )
    .onFailure(e -> PgErrors.duplicate(e))
    .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
        .id(tree.getId())
        .isModified(false)
        .target(tree)
        .status(UpsertStatus.OK)
        .message(ImmutableMessage.builder()
            .text(new StringBuilder()
                .append("Tree with id:")
                .append(" '").append(tree.getId()).append("'")
                .append(" is already saved.")
                .toString())
            .build())
        .build())
    .onFailure().invoke(e -> PgErrors.deadEnd("Can't insert into "
        +"\r\n"
        + "'TREE': " + treeInsert.getValue() 
        + "\r\n"
        + "  and/or"
        + "\r\n "
        + "'TREE_VALUE' : '" + treeValueInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> commit(Commit commit) {
    final var commitsInsert = sqlBuilder.commits().insertOne(commit);
    return client.preparedQuery(commitsInsert.getValue()).execute(commitsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(commit.getId())
            .isModified(true)
            .target(commit)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Commit with id:")
                    .append(" '").append(commit.getId()).append("'")
                    .append(" has been saved.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> PgErrors.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(commit.getId())
            .isModified(false)
            .target(commit)
            .status(UpsertStatus.CONFLICT)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Commit with id:")
                    .append(" '").append(commit.getId()).append("'")
                    .append(" is already saved.")
                    .toString())
                .build())
            .build())
        .onFailure().invoke(e -> PgErrors.deadEnd("Can't insert into 'COMMIT': '" + commitsInsert.getValue() + "'!", e));
  }
  
}
