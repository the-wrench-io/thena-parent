export type TaskId = string;


export interface ProgramMessage {
  id: string, msg: string
}

export interface Task {
  version: string;
  created: string;
  updated: string | undefined;
  type: 'TASK';
  id: TaskId;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  roles: string[];
  owners: string[];
  labels: string[];
  subject: string;
  description: string;
  extensions: TaskExtension[];
  externalComments: TaskComment[];
  internalComments: TaskComment[];
}


export interface TaskExtension {
  id: string;
  type: 'dialob' | 'upload' | string;
  body: string;
  name: string;
}

export interface TaskComment {
  id: string;
  created: Date;
  replyToId?: string;
  commentText: string;
  username: string;
}

export type TaskStatus = 'CREATED' | 'IN_PROGRESS' |'COMPLETED' | 'REJECTED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';


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
  org(): Promise<{org: Org, user: User}>;
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

