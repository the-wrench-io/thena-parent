import { Client, Store, HeadState, CreateBuilder, TaskId, Task } from './client-types';
import { } from './client-store';


type BackendInit = { created: boolean } | null


export class ServiceImpl implements Client {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  get config() {
    return this._store.config;
  }
  async head(): Promise<HeadState> {
    try {
      const init = await this._store.fetch<BackendInit>("init", { notFound: () => null });    
      if(init === null) {
        return { name: "", contentType: "BACKEND_NOT_FOUND" };
      }
      
      return { name: "", contentType: "OK" };
    } catch(error) {
      console.error(error);
      return { name: "", contentType: "NO_CONNECTION" };
    }
    
  }
  create(): CreateBuilder {
    const head = () => this._store.fetch<HeadState>("head", { method: "POST", body: JSON.stringify({}) });
    const migrate: (init: object) => Promise<HeadState> = (init) => this._store.fetch<HeadState>("migrate", { method: "POST", body: JSON.stringify(init) })
    return { head, migrate };
  }
  task(id: TaskId): Promise<Task> {
    return this._store.fetch<Task>(`tasks/${id}`);
  }
}