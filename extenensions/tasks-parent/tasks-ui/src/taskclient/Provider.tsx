import React from 'react';

import { initSession, SessionData } from './session';
import ActionsImpl from './actions';
import { Client, HeadState } from './client-types';
import { ClientContext, ComposerContext } from './client-ctx';
import RequireProject from './Components/RequireProject';
import { TasksProvider } from './tasks-ctx';

const Provider: React.FC<{ children: React.ReactNode, service: Client, head?: HeadState }> = ({ children, service, head }) => {
  const [session, dispatch] = React.useState<SessionData>(initSession);

  const actions = React.useMemo(() => {
    console.log("init ide dispatch");
    return new ActionsImpl(dispatch, service);
  }, [dispatch, service]);

  const contextValue = React.useMemo(() => {
    console.log("init ide context value");
    return { session, actions };
  }, [session, actions]);

  React.useLayoutEffect(() => {
    console.log("init ide data");
    if (head) {
      actions.handleLoadHead(head);
    } else {
      actions.handleLoad();
    }
  }, [actions, head]);

  return (
    <ClientContext.Provider value={service}>
      <ComposerContext.Provider value={contextValue}>
        <TasksProvider backend={service}>
          {session.head.contentType === 'NOT_CREATED' ? <RequireProject /> : undefined}
          {children}
        </TasksProvider>
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};
export default Provider;
