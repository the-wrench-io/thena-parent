package io.resys.thena.tasks.client.spi.actions;

import io.resys.thena.tasks.client.api.actions.ExportActions;
import io.resys.thena.tasks.client.api.model.Export;
import io.resys.thena.tasks.client.api.model.ImmutableExport;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExportActionsImpl implements ExportActions {

  @Override
  public ExportQuery export() {

    return new ExportQuery() {
      private String name;
      private LocalDate startDate;
      private LocalDate endDate;

      @Override
      public ExportQuery name(String name) {
        this.name = name;
        return this;
      }
      @Override
      public ExportQuery startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
      }
      @Override
      public ExportQuery endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
      }
      @Override
      public Uni<Export> build() {
        return Uni.createFrom().item(ImmutableExport.builder()
            .id(String.valueOf(UUID.fromString(name))) // check id generation method
            .name(String.valueOf(this.hashCode())) // check hash generation method
            .startDate(startDate)
            .endDate(endDate)
            .created(LocalDateTime.now())
            .addAllEntries(null) // call findInDateRange
            .statistics(null) // wait for statistics implementation
            .build());
      }
    };
  }
}
