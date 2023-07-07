export type TaskId = string;



export interface Task {
  readonly created: string;
  readonly updated: string;
  readonly archived: string | undefined;
  readonly dueDate: string | undefined;

  readonly parentId: string | undefined;
  readonly transactions: TaskTransaction[];
  readonly roles: string[];
  readonly assigneeIds: string[];
  readonly reporterId: string;

  readonly title: string;
  readonly description: string;
  readonly priority: TaskPriority;
  readonly status: TaskStatus;
  readonly labels: string[];
  readonly extensions: TaskExtension[];
  readonly comments: TaskComment[];

  readonly id: TaskId;
  readonly version: string;
  readonly documentType: 'TASK';
}

export interface TaskVersion {
  task: Task[];
}

export type TaskStatus = 'CREATED' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';


export interface TaskTransaction {
  id: string;
  commands: TaskCommand[];
}

export interface TaskExtension {
  id: string;
  type: 'dialob' | 'upload' | string;
  name: string;
  body: string;
}

export interface TaskComment {
  id: string;
  created: Date;
  replyToId: string | undefined;
  commentText: string;
  username: string;
}

export interface TaskHistory {
  id: string;
  versions: TaskVersion[];
}

export interface TaskCommand {
  targetDate: string | undefined;
  userId: string;
  commandType: TaskCommandType;
}

export type TaskCommandType =
  'CreateTask' | 'ChangeTaskStatus' | 'ChangeTaskPriority' | 'AssignTaskReporter' | 'ArchiveTask' |
  'CommentOnTask' | 'AssignTaskRoles' | 'AssignTask' | 'ChangeTaskDueDate' | 'AssignTaskParent' |
  'ChangeTaskInfo' | 'ChangeTaskExtension';

export interface TaskUpdateCommand<T extends TaskCommandType> extends TaskCommand {
  taskId: TaskId;
  commandType: T;
}

export interface CreateTask extends TaskCommand {
  commandType: 'CreateTask';
  roles: string[];
  assigneeIds: string[];
  reporterId: string;
  status: TaskStatus | undefined;
  dueDate: string | undefined;
  priority: TaskPriority;
  labels: string[];
  extensions: TaskExtension[];
  comments: TaskComment[];
}

export interface AssignTaskReporter extends TaskUpdateCommand<'AssignTaskReporter'> {
  reporterId: string;
}

export interface ArchiveTask extends TaskUpdateCommand<'ArchiveTask'> {
}

export interface ChangeTaskStatus extends TaskUpdateCommand<'ChangeTaskStatus'> {
  status: TaskStatus;
}

export interface ChangeTaskPriority extends TaskUpdateCommand<'ChangeTaskPriority'> {
  priority: TaskPriority;
}

export interface AssignTaskParent extends TaskUpdateCommand<'AssignTaskParent'> {
  parentId: string;
}

export interface CommentOnTask extends TaskUpdateCommand<'CommentOnTask'> {
  commentId: string | undefined;
  replyToCommentId: string | undefined;
  commentText: string;
}

export interface AssignTaskRoles extends TaskUpdateCommand<'AssignTaskRoles'> {
  roles: string[];
}

export interface AssignTask extends TaskUpdateCommand<'AssignTask'> {
  assigneeIds: string[];
}

export interface ChangeTaskDueDate extends TaskUpdateCommand<'ChangeTaskDueDate'> {
  dueDate: string;
}

export interface ChangeTaskInfo extends TaskUpdateCommand<'ChangeTaskInfo'> {
  title: string;
  description: string
}

export interface ChangeTaskExtension extends TaskUpdateCommand<'ChangeTaskExtension'> {
  id: TaskId | undefined;
  type: string;
  body: string;
}


