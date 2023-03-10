package io.resys.thena.docdb.spi.commits;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.smallrye.mutiny.Uni;

public interface CommitCommands {
  
  
  Uni<CommitResult> ex();

}
