package io.resys.thena.tasks.client.spi.visitors;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.tasks.client.api.model.Export.ExportEvent;
import io.resys.thena.tasks.client.api.model.Export.ExportEventType;
import io.resys.thena.tasks.client.api.model.ImmutableExportEvent;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;

public class ExportVisitor {
  private final List<ExportEvent> events = new ArrayList<>();
  private final Map<String, Integer> ids = new HashMap<>();

  public ExportVisitor visitTasks(List<Task> tasks) {
    tasks.forEach(task -> {
      this.vistTaskCreated(task);
      this.vistTaskActions(task);
    });
    return this;
  }
  
  private ExportVisitor vistTaskActions(Task task) {
    final var previous = new ArrayList<TaskCommand>();
    for(final var tx : task.getTransactions()) {
      for(final var command : tx.getCommands()) {
        visitTaskAction(task, command, previous);
        previous.add(command);
      }
    }
    return this;
  }
  
  private ExportVisitor visitTaskAction(Task task, TaskCommand command, List<TaskCommand> previous) {
    if(previous.isEmpty()) {
      return this;
    }
    if(command instanceof ChangeTaskStatus) {
      return visitChangeTaskStatus(task, (ChangeTaskStatus) command, previous);
    } 
        
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_UPDATED)
        .eventDate(command.getTargetDate().toLocalDate())
        .build());
    return this;
  }
  
  private ExportVisitor visitChangeTaskStatus(Task task, ChangeTaskStatus command, List<TaskCommand> previous) {
    
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_STATUS_SET)
        .eventDate(command.getTargetDate().toLocalDate())
        .taskStatus(command.getStatus())
        .build());
    return this;
  }
  
  private ExportVisitor vistTaskCreated(Task task) {
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_STATUS_SET)
        .eventDate(task.getCreated().toLocalDate())
        .taskStatus(Task.Status.CREATED)
        .build());
    return this;
  }
  
  private String allocateId(Task task) {
    if(ids.containsKey(task.getId())) {
      var old = ids.get(task.getId());
      ids.put(task.getId(), ++ old);
    } else {
      ids.put(task.getId(), 1);
    }

    return ids.get(task.getId()) + "";
  }

  public List<ExportEvent> build() {
    return events;
  }
}
