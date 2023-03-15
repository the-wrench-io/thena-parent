import { Task } from './client-types'


interface TaskDescriptor {
  entry: Task;
  created: Date;
  id: string;
  subject: string,
  priority,
  status,
  owner,
  duedate,
  roles,
}

interface TaskDescriptors {
  getDescriptorById(taskId: string): TaskDescriptor | undefined;
  findAll(): TaskDescriptor[];
}

class TaskDescriptorImpl implements TaskDescriptor {
  private _entry: Task;
  private _created: Date;
  
  constructor(entry: Task) {
    this._entry = entry;
    this._created = new Date(entry.created);
  }
  get id() { return this._entry.id }
  get entry() { return this._entry }
  get created() { return this._created }
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
}


export type { TaskDescriptor, TaskDescriptors };
export { TaskDescriptorsImpl };



