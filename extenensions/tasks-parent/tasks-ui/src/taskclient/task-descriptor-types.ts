import { Task, TaskStatus, TaskPriority } from './client-types'


interface TaskDescriptor {
  entry: Task;
  created: Date;
  id: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: Date;
  roles: string[];
  owners: string[];
  labels: string[];
  subject: string;
  description: string;
}

interface TaskDescriptors {
  getDescriptorById(taskId: string): TaskDescriptor | undefined;
  findAll(): TaskDescriptor[];
}

class TaskDescriptorImpl implements TaskDescriptor {
  private _entry: Task;
  private _created: Date;
  private _dueDate: Date | undefined;
    
  constructor(entry: Task) {
    this._entry = entry;
    this._created = new Date(entry.created);
    this._dueDate = entry.dueDate ? new Date(entry.dueDate) : undefined;
  }
  get id() { return this._entry.id }
  get entry() { return this._entry }
  get created() { return this._created }
  get dueDate() { return this._dueDate }
  
  get status() { return this._entry.status }
  get priority() { return this._entry.priority }
  get roles() { return this._entry.roles }
  get owners() { return this._entry.owners }
  get labels() { return this._entry.labels }
  get subject() { return this._entry.subject }
  get description() { return this._entry.description }
  
}

class TaskDescriptorsImpl implements TaskDescriptors {
  private _values: Task[];
  private _defs: Record<string, TaskDescriptor>;

  constructor(values: Task[], defs: Record<string, TaskDescriptor> | undefined) {
    this._values = values;
    this._defs = defs ? defs : {};
    if(!defs) {
      this._values
        .map(value => new TaskDescriptorImpl(value))
        .forEach(value => this._defs[value.entry.id] = value)
    }
  }

  getDescriptorById(id: string): TaskDescriptor | undefined {
    if (this._defs[id]) {
      return this._defs[id];
    }
    
    return undefined;
  }
  findAll() {
    return Object.values(this._defs);
  }
  withValues(values: Task[]) {
    return new TaskDescriptorsImpl(values, undefined);
  }
}


export type { TaskDescriptor, TaskDescriptors };
export { TaskDescriptorsImpl };



