package io.resys.thena.tasks.client.spi.actions;

import java.time.LocalDate;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.ArchivedTasksQuery;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.GetArchivedTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true, chain = true)
@Getter(AccessLevel.NONE)
@RequiredArgsConstructor
@AllArgsConstructor
public class ArchivedTasksQueryImpl implements ArchivedTasksQuery {
  private final DocumentStore ctx;
  private String title;
  private String description;
  private String reporterId;

  @Override
  public Uni<List<Task>> build(LocalDate fromCreatedOrUpdated) {
    return ctx.getConfig().accept(new GetArchivedTasksVisitor(title, description, reporterId, fromCreatedOrUpdated));
  }

}
