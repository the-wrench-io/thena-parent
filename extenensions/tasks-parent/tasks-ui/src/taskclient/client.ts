import { Client, Store, HeadState, CreateBuilder, TaskId, Task, TaskPagination, Org, User } from './client-types';
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
      if (init === null) {
        return { name: "", contentType: "BACKEND_NOT_FOUND" };
      }

      return { name: "", contentType: "OK" };
    } catch (error) {
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
  async org(): Promise<{ org: Org, user: User }> {
    return {
      org: {
        owners: [
          "sam vimes",
          "lord vetinari",
          "lady sybil vimes",
          "carrot ironfoundersson",
          "nobby nobbs"
        ],
        roles: [
          "admin-role",
          "water-department",
          "education-department",
          "elderly-care-department",
          "sanitization-department"
        ]
      },
      user: {
        displayName: "Sam Vimes",
        userId: "sam vimes",
        userRoles: ["admin-role"]
      }
    };
  }

  async active(): Promise<TaskPagination> {
    const tasks = await this._store.fetch<object[]>(`active/tasks`);
    return {
      page: 1,
      total: { pages: 1, records: tasks.length },
      records: tasks as any
    }
  }
}