import { ServiceImpl as ServiceImplAs } from './client';
import { DefaultStore as DefaultStoreAs } from './client-store';

import { TablePagination, TablePagination as TablePaginationAs } from './table-pagination';

import {
  ClientEntity, Project,
  HeadState,
  ClientError, CreateBuilder, Client, StoreConfig, Store,
  TaskId, ProjectId, Task, ClientEntityType, ProgramMessage
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
import { ClientContextType, ComposerContextType } from './Components/Context';
import * as Context from './Components/Context';
import * as Hooks from './hooks';


declare namespace DeClient {
  export type { TablePagination };
  export type { ClientContextType, ComposerContextType };
  export type {
    ClientEntity, Project, TaskId, Task, HeadState,
    ClientError, CreateBuilder, Client, StoreConfig, Store,
    ProjectId, ClientEntityType,
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
}


namespace DeClient {
  export const TablePaginationImpl = TablePaginationAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
  export const Error = ErrorView;
  export const Provider = Context.Provider;
  export const useService = Hooks.useService;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
}

export default DeClient;