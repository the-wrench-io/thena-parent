export type TaskId = string;

export type ClientEntityType = 'TASK';

export interface ProgramMessage {
  id: string, msg: string
}

export interface ClientEntity<T extends string> {
  id: T;
  version: string;
  created: string;
  updated: string | undefined;
  type: ClientEntityType;
}
export interface Task extends ClientEntity<TaskId> {
  id: TaskId;
  status: Status;
  priority: Priority;
  dueDate: string;
  roles: string[];
  owners: string[];
  labels: string[];
  subject: string;
  description: string;
  externalComments: TaskComment[];
  internalComments: TaskComment[];
}

export interface TaskComment {
  
}

export type Status = 'CREATED' | 'IN_PROGRESS' |'COMPLETED' | 'REJECTED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

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

