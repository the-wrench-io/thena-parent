export type TaskId = string;
export type ProjectId = string;
export type ClientEntityType = 'PROJECT' | 'TASK';

export interface ProgramMessage {
  id: string, msg: string
}

export interface ClientEntity<T extends string> {
  id: T;
  version: string;
  created: string;
  updated: string;
  type: ClientEntityType;
}
export interface Project extends ClientEntity<ProjectId> {
  id: ProjectId;
}
export interface Task extends ClientEntity<TaskId> {
  id: TaskId;
}

export interface HeadState {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
  
  projects: Project[],
  tasks: Task[],
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

