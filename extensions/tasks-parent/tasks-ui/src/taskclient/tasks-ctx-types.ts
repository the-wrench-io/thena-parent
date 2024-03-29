import { Task, TaskPriority, TaskStatus, TaskExtension } from './client-types';

export interface TaskDescriptor {
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
  dialobId: string;
  description: string;
  uploads: TaskExtension[]
}

export interface PalleteType {
  priority: {
    'HIGH': string,
    'LOW': string,
    'MEDIUM': string
  },
  status: {
    'REJECTED': string,
    'IN_PROGRESS': string,
    'COMPLETED': string,
    'CREATED': string,
  },
  mywork: {
    review: string,
    upload: string
  },
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export interface TasksContextType {
  setState: TasksDispatch;
  loading: boolean;
  state: TasksState,
  pallette: PalleteType;
}

export type RoleUnassigned = "_nobody_";
export type OwnerUnassigned = "_nobody_";

export type TasksMutator = (prev: TasksMutatorBuilder) => TasksMutatorBuilder;
export type TasksDispatch = (mutator: TasksMutator) => void;

export type GroupBy = 'status' | 'owners' | 'roles' | 'priority' | 'none';
export type FilterByStatus = { type: 'FilterByStatus', status: TaskStatus[], disabled: boolean }
export type FilterByPriority = { type: 'FilterByPriority', priority: TaskPriority[], disabled: boolean }
export type FilterByOwners = { type: 'FilterByOwners', owners: string[], disabled: boolean }
export type FilterByRoles = { type: 'FilterByRoles', roles: string[], disabled: boolean }
export type FilterBy = FilterByStatus | FilterByPriority | FilterByOwners | FilterByRoles;

export interface Group {
  id: string;
  type: GroupBy;
  color?: string;
  records: TaskDescriptor[];
}

export interface TasksState {
  tasks: TaskDescriptor[];
  tasksByOwner: Record<string, TaskDescriptor[]>;
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;
  pallette: TasksStatePallette;
}

export interface TasksStatePallette {
  roles: Record<string, string>;
  owners: Record<string, string>;
  status: Record<string, string>;
  priority: Record<string, string>;
}

export interface TasksMutatorBuilder extends TasksState {
  withTasks(tasks: Task[]): TasksMutatorBuilder;
  withSearchString(searchString: string): TasksMutatorBuilder;
  withGroupBy(groupBy: GroupBy): TasksMutatorBuilder;
  withFilterByStatus(status: TaskStatus[]): TasksMutatorBuilder;
  withFilterByPriority(priority: TaskPriority[]): TasksMutatorBuilder;
  withFilterByOwner(owners: string[]): TasksMutatorBuilder;
  withFilterByRoles(roles: string[]): TasksMutatorBuilder;
}


