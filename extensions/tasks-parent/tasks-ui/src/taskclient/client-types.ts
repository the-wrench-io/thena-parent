import { ProfileStore } from './profile-types';
import { Task, TaskId } from './task-types';

export interface ProgramMessage {
  id: string, msg: string
}

export interface Org {
  owners: string[];
  roles: string[];
}

export interface User {
  userId: string;
  userRoles: string[];
  displayName: string;
}

export interface ClientError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}


export interface Client {
  config: StoreConfig;
  profile: ProfileStore;

  active(): Promise<TaskPagination>
  org(): Promise<{ org: Org, user: User }>;
  task(id: TaskId): Promise<Task>
}
export interface StoreConfig {
  url: string;
  oidc?: string;
  status?: string;
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  fetch<T>(path: string, init?: RequestInit & { notFound?: () => T }): Promise<T>;
}

export interface TaskPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: Task[];
}

