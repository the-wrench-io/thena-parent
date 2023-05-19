import Burger from '@the-wrench-io/react-burger';

import * as Client from './client-types';


export type DocumentId = string; 

export type Document = 
 { kind: 'HEAD', id: string, delegate: Client.HeadState } | 
 { kind: 'TASK', id: string, delegate: Client.Task };

export type DocumentUpdate = {};


export interface TabEntity {
  value?: string | null;
}

export interface TabBody {
  nav?: TabEntity
  withNav(nav: TabEntity): TabBody;
}
export interface Tab extends Burger.TabSession<TabBody> { }

export interface PageUpdate {
  saved: boolean;
  origin: Document;
  value: DocumentUpdate[];
  withValue(value: DocumentUpdate): PageUpdate;
}

export interface Session {
  head: Client.HeadState,
  pages: Record<DocumentId, PageUpdate>;

  getEntity(id: DocumentId): undefined | Document;

  withPage(page: DocumentId): Session;
  withPageValue(page: DocumentId, value: DocumentUpdate[]): Session;
  withoutPages(pages: DocumentId[]): Session;

  withHead(site: Client.HeadState): Session;
}

export interface Actions {
  handleLoad(): Promise<void>;
  handleLoadHead(site?: Client.HeadState): Promise<void>;
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void;
  handlePageUpdateRemove(pages: DocumentId[]): void;
}




