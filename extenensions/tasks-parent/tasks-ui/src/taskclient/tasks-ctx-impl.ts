import { Task, TaskPriority, TaskStatus } from './client-types';
import {
  PalleteType, TasksState, TasksMutatorBuilder, TaskDescriptor, FilterBy, Group, GroupBy,
  RoleUnassigned, OwnerUnassigned, TasksStatePallette
} from './tasks-ctx-types';



const _nobody_: RoleUnassigned & OwnerUnassigned = '_nobody_';

// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
const bittersweet: string = '#FF595E'; //red
const emerald: string = '#26C485';     //green
const sunglow: string = '#FFCA3A';     //yellow
const steelblue: string = '#1982C4';   //blue
const ultraviolet: string = '#6A4C93'; //lillac

const Pallette: PalleteType = {
  priority: {
    'HIGH': bittersweet,
    'LOW': steelblue,
    'MEDIUM': emerald
  },
  status: {
    'REJECTED': bittersweet,
    'IN_PROGRESS': emerald,
    'COMPLETED': steelblue,
    'CREATED': ultraviolet,
  },
  colors: { red: bittersweet, green: emerald, yellow: sunglow, blue: steelblue, violet: ultraviolet }
};


interface ExtendedInit extends TasksState {
  filtered: TaskDescriptor[];
  owners: string[];
  roles: string[];
  pallette: {
    roles: Record<string, string>
    owners: Record<string, string>
    status: Record<string, string>
    priority: Record<string, string>
  }
}

class TasksStateBuilder implements TasksMutatorBuilder {
  private _tasks: TaskDescriptor[];
  private _filtered: TaskDescriptor[];
  private _groupBy: GroupBy;
  private _groups: Group[];
  private _filterBy: FilterBy[];
  private _searchString: string | undefined;
  private _owners: string[];
  private _roles: string[];
  private _pallette: TasksStatePallette;

  constructor(init: ExtendedInit) {
    this._tasks = init.tasks;
    this._groupBy = init.groupBy;
    this._groups = init.groups;
    this._filterBy = init.filterBy;
    this._searchString = init.searchString;
    this._filtered = init.filtered;
    this._owners = init.owners;
    this._roles = init.roles;
    this._pallette = init.pallette;
  }
  get pallette(): TasksStatePallette { return this._pallette };
  get owners(): string[] { return this._owners };
  get roles(): string[] { return this._roles };
  get tasks(): TaskDescriptor[] { return this._tasks };
  get groupBy(): GroupBy { return this._groupBy };
  get groups(): Group[] { return this._groups };
  get filterBy(): FilterBy[] { return this._filterBy };
  get searchString(): string | undefined { return this._searchString };
  get filtered(): TaskDescriptor[] { return this._filtered };

  withTasks(input: Task[]): TasksStateBuilder {
    const tasks: TaskDescriptor[] = [];
    const roles: string[] = [_nobody_];
    const owners: string[] = [_nobody_];
    input.forEach(task => {
      tasks.push(new TaskDescriptorImpl(task));
      task.roles.forEach(role => {
        if (!roles.includes(role)) {
          roles.push(role)
        }
      });
      task.owners.forEach(owner => {
        if (!owners.includes(owner)) {
          owners.push(owner)
        }
      });
    });

    owners.sort();
    roles.sort();

    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._roles, owners: this._owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of tasks) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    const pallette: TasksStatePallette = {
      roles: {},
      owners: {},
      status: Pallette.status,
      priority: Pallette.priority
    }
    withColors(roles).forEach(e => pallette.roles[e.value] = e.color);
    withColors(owners).forEach(e => pallette.owners[e.value] = e.color);

    return new TasksStateBuilder({ ...this.clone(), 
      groupBy: this._groupBy,
      roles, owners, 
      pallette, 
      tasks, 
      filtered, 
      groups: grouping.build() });
  }
  withGroupBy(groupBy: GroupBy): TasksStateBuilder {
    const grouping = new GroupVisitor({ groupBy, roles: this._roles, owners: this._owners });
    this._filtered.forEach(value => grouping.visit(value))
    
    return new TasksStateBuilder({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByStatus(status: TaskStatus[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByStatus', status, disabled: false });
  }
  withFilterByPriority(priority: TaskPriority[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByPriority', priority, disabled: false });
  }
  withFilterByOwner(owners: string[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByOwners', owners, disabled: false });
  }
  withFilterByRoles(roles: string[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }

  clone(): ExtendedInit {
    const init = this;
    return {
      tasks: init.tasks,
      groupBy: init.groupBy,
      groups: init.groups,
      filterBy: init.filterBy,
      searchString: init.searchString,
      filtered: init.filtered,
      owners: init.owners,
      roles: init.roles,
      pallette: init.pallette,
    }
  }


  private withFilterBy(input: FilterBy): TasksStateBuilder {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._roles, owners: this._owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._tasks) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TasksStateBuilder({ ...this.clone(), filterBy, filtered, groups: grouping.build() });
  }

  private createFilters(input: FilterBy): FilterBy[] {
    let filter = this._filterBy.find(v => v.type === input.type);
    // not created
    if (!filter) {
      return [...this._filterBy, input];
    }

    const result: FilterBy[] = [];
    for (const v of this._filterBy) {
      if (v.type === input.type) {
        result.push(input);
      } else {
        result.push(v);
      }
    }
    return result;
  }
}


class GroupVisitor {
  private _groupBy: GroupBy;
  private _groups: Record<string, Group>;
  constructor(init: {
    groupBy: GroupBy;
    roles: string[];
    owners: string[];
  }) {
    this._groupBy = init.groupBy;
    this._groups = {};
    console.log("INIT", init);
    
    if (init.groupBy === 'owners') {
      withColors(init.owners).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'roles') {
      withColors(init.roles).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'priority') {
      const values: TaskPriority[] = ['HIGH', 'LOW', 'MEDIUM'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.priority[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'status') {
      const values: TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.status[o], id: o, type: init.groupBy })
    }
  }

  public build(): Group[] {
    return Object.values(this._groups);
  }

  public visit(task: TaskDescriptor) {
    if (this._groupBy === 'owners') {
      if (task.owners.length) {
        task.owners.forEach(o => this._groups[o].records.push(task));
      } else {
        this._groups[_nobody_].records.push(task);
      }
    } else if (this._groupBy === 'roles') {
      if (task.roles.length) {
        task.roles.forEach(o => this._groups[o].records.push(task));
      } else {
        this._groups[_nobody_].records.push(task);
      }
    } else if (this._groupBy === 'status') {
      this._groups[task.status].records.push(task);
    } else if (this._groupBy === 'priority') {
      this._groups[task.priority].records.push(task);
    }
  }
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

function applyDescFilters(desc: TaskDescriptor, filters: FilterBy[]): boolean {
  for (const filter of filters) {
    if (filter.disabled) {
      continue;
    }
    if (!applyDescFilter(desc, filter)) {
      return false;
    }
  }

  return true;
}

function applyDescFilter(desc: TaskDescriptor, filter: FilterBy): boolean {
  switch (filter.type) {
    case 'FilterByOwners': {
      for (const owner of filter.owners) {
        if (desc.owners.length === 0 && owner === _nobody_) {
          continue;
        }
        if (!desc.owners.includes(owner)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByRoles': {
      for (const role of filter.roles) {
        if (desc.roles.length === 0 && role === _nobody_) {
          continue;
        }
        if (!desc.roles.includes(role)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByStatus': {
      return filter.status.includes(desc.status);
    }
    case 'FilterByPriority': {
      return filter.priority.includes(desc.priority);
    }
  }
  // @ts-ignore
  throw new Error("unknow filter" + filter)
}

function withColors<T>(input: T[]): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  const colors = Object.values(Pallette.colors);
  let index = 0;
  for (const value of input) {
    result.push({ value, color: colors[index] })
    if (colors.length - 1 === index) {
      index = 0;
    } else {
      index++;
    }
  }

  return result;
}


export { TaskDescriptorImpl, TasksStateBuilder, Pallette };
export type { };
