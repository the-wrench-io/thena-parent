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


export interface HeadState {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
}

export interface ClientError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}

export interface CreateBuilder {
  head(): Promise<HeadState>;
  migrate(init: object): Promise<{}>;
}

export interface Client {
  config: StoreConfig;
  create(): CreateBuilder;
  head(): Promise<HeadState>
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

