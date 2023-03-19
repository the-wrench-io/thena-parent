import { ServiceImpl as ServiceImplAs } from './client';
import { DefaultStore as DefaultStoreAs } from './client-store';

import { TablePagination, TablePagination as TablePaginationAs } from './table-pagination';

import {
  ClientEntity,
  HeadState,
  ClientError, CreateBuilder, Client, StoreConfig, Store,
  TaskId, Task, TaskPriority, TaskStatus, ClientEntityType, ProgramMessage
} from './client-types';

import {
  DocumentId, Document, DocumentUpdate,
  TabEntity, TabBody, Tab,
  PageUpdate, Session, Actions,
} from './composer-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';


import ErrorView from './Components/ErrorView';
import { ClientContextType, ComposerContextType } from './client-ctx';
import ProviderImpl from './Provider';

import { 
  TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
  PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
  TasksStatePallette
} from './tasks-ctx-types';


import * as Hooks from './hooks';


declare namespace TaskClient {
  export type { TablePagination };
  export type { ClientContextType, ComposerContextType };
  export type {
    ClientEntity, TaskId, Task, TaskPriority, TaskStatus, HeadState,
    ClientError, CreateBuilder, Client, StoreConfig, Store,
    ClientEntityType,
    ProgramMessage
  }

  export type {
    DocumentId, Document, DocumentUpdate,
    TabEntity, TabBody, Tab,
    PageUpdate, Session, Actions
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }

  export type {
    TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
    PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
    TasksStatePallette
  }
}


namespace TaskClient {
  export const TablePaginationImpl = TablePaginationAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
  export const Error = ErrorView;
  export const Provider = ProviderImpl;
  export const useBackend = Hooks.useBackend;
  export const useTasks = Hooks.useTasks;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
}

export default TaskClient;