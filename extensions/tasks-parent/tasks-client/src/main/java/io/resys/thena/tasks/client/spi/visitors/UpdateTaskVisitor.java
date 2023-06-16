package io.resys.thena.tasks.client.spi.visitors;

import java.util.ArrayList;

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

import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskReporter;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskCommandType;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;

public class UpdateTaskVisitor {
  private final Task start;
  private final List<TaskCommand> visitedCommands = new ArrayList<>();
  private ImmutableTask current;
  
  public UpdateTaskVisitor(Task start) {
    this.start = start;
    this.current = ImmutableTask.builder().from(start).build();
  }
  
  
  
  public UpdateTaskVisitor visit(TaskUpdateCommand command) {    
    visitedCommands.add(command);
    if(command.getCommandType() == TaskCommandType.ChangeTaskStatus) {
      return visitChangeTaskStatus((ChangeTaskStatus) command);
    } else if(command.getCommandType() == TaskCommandType.ChangeTaskPriority) {
      return visitChangeTaskPriority((ChangeTaskPriority) command);
    } else if(command.getCommandType() == TaskCommandType.AssignTaskReporter) {
      return visitAssignTaskReporter((AssignTaskReporter) command);
    }
    
    throw new UpdateTaskVisitorException(String.format("Unsupport command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }

  public UpdateTaskVisitor visit(List<TaskUpdateCommand> commands) {
    commands.forEach(this::visit);
    return this;
  }
  
  private UpdateTaskVisitor visitChangeTaskStatus(ChangeTaskStatus command) {
    this.current = this.current
        .withStatus(command.getStatus())
        .withUpdated(command.getTargetDate());
    return this;
  }
  
  private UpdateTaskVisitor visitChangeTaskPriority(ChangeTaskPriority command) {
    this.current = this.current
        .withPriority(command.getPriority())
        .withUpdated(command.getTargetDate());
    return this;
  }
    
  private UpdateTaskVisitor visitAssignTaskReporter(AssignTaskReporter command) {
    this.current = this.current
        .withReporterId(command.getReporterId())
        .withUpdated(command.getTargetDate());
    return this;
  }
  


  public Task build() {
    final var transactions = new ArrayList<>(start.getTransactions());
    transactions.add(ImmutableTaskTransaction.builder().id(String.valueOf(transactions.size() +1)).commands(visitedCommands).build());
    return this.current.withTransactions(transactions);
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
