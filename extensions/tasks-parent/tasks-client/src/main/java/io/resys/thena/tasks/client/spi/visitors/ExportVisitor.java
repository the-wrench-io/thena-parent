package io.resys.thena.tasks.client.spi.visitors;

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
