package io.resys.thena.docdb.file.spi;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaObject.Tree;
import io.resys.thena.docdb.file.FileBuilder.TreeFileBuilder;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.ImmutableTreeTableRow;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.spi.ClientCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeFileBuilderImpl implements TreeFileBuilder {

  private final ClientCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create TREE table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getTrees().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from TREE table")
        .command((root) -> {
          return root.getRepoTable(ctx).getTrees().getRows();
        })
        .build();
  }
  @Override
  public FileTuple getById(String id) {
    return ImmutableFileTuple.builder()
        .value("Select by id from TREE table")
        .props(Tuple.of(id))
        .command((root) -> {
          return root.getRepoTable(ctx).getTrees().getRows().stream()
              .filter(tree -> tree.getId().equals(id))
              .collect(Collectors.toList());
        })
        .build();
  }
  @Override
  public FileTuple insertOne(Tree src) {
    return ImmutableFileTuple.builder()
        .value("Insert row into TREE table")
        .props(Tuple.of(src.getId()))
        .command((root) -> {
          final var exists = root.getRepoTable(ctx).getTrees().getRows().stream()
              .filter(tree -> tree.getId().equals(src.getId()))
              .collect(Collectors.toList());
          if(!exists.isEmpty()) {
            return exists;
          }
          final var newRow = root.getRepoTable(ctx).getTrees().insert(ImmutableTreeTableRow.builder()
              .id(src.getId())
              .build());
          
          return Arrays.asList(newRow);
        })
        .build();
  }
}
