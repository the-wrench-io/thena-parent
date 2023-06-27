package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

/*-
 * #%L
 * thena-tasks-client
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
import java.util.Optional;

import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskReporter;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;

public class TaskCommandVisitor {
  private final DocumentConfig ctx;
  private final Task start;
  private final List<TaskCommand> visitedCommands = new ArrayList<>();
  private ImmutableTask current;
  
  public TaskCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public TaskCommandVisitor(Task start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableTask.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public Task visitTransaction(List<? extends TaskCommand> commands) {
    commands.forEach(this::visitCommand);
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableTaskTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private Task visitCommand(TaskCommand command) {    
    visitedCommands.add(command);  
    switch (command.getCommandType()) {
    case AssignTaskReporter: 
      return visitAssignTaskReporter((AssignTaskReporter) command);
    case ChangeTaskPriority:
      return visitChangeTaskPriority((ChangeTaskPriority) command);
    case ChangeTaskStatus:
      return visitChangeTaskStatus((ChangeTaskStatus) command);
    case CreateTask:
      return visitCreateTask((CreateTask)command);
    }
    throw new UpdateTaskVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }

  
  private Task visitCreateTask(CreateTask command) {
    final var gen = ctx.getGid();
    final var targetDate = Optional.ofNullable(command.getTargetDate()).orElseGet(() -> LocalDateTime.now());
    this.current = ImmutableTask.builder()
        .id(gen.getNextId(DocumentType.TASK))
        .version(gen.getNextVersion(DocumentType.TASK))
        .addAllAssigneeIds(command.getAssigneeIds().stream().distinct().toList())
        .addAllRoles(command.getRoles().stream().distinct().toList())
        .reporterId(command.getReporterId())
        .labels(command.getLabels().stream().distinct().toList())
        .extensions(command.getExtensions())
        .comments(command.getComments())
        .title(command.getTitle())
        .description(command.getDescription())
        .priority(command.getPriority())
        .dueDate(command.getDueDate())
        .created(targetDate)
        .updated(targetDate)
        .status(command.getStatus() == null ? Status.CREATED : command.getStatus())
        .addTransactions(ImmutableTaskTransaction.builder().id(String.valueOf(1)).addCommands(command).build())
        .build();
    return this.current;
  }
  
  
  private Task visitChangeTaskStatus(ChangeTaskStatus command) {
    this.current = this.current
        .withStatus(command.getStatus())
        .withUpdated(command.getTargetDate());
    return this.current;
  }
  
  private Task visitChangeTaskPriority(ChangeTaskPriority command) {
    this.current = this.current
        .withPriority(command.getPriority())
        .withUpdated(command.getTargetDate());
    return this.current;
  }
    
  private Task visitAssignTaskReporter(AssignTaskReporter command) {
    this.current = this.current
        .withReporterId(command.getReporterId())
        .withUpdated(command.getTargetDate());
    return this.current;
  }
  
  
  public static class UpdateTaskVisitorException extends RuntimeException {

    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateTaskVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateTaskVisitorException(String message) {
      super(message);
    }
  }
}
