import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';

import Activities from './Activities';
import Tasks from './Tasks';
import MyWork from './MyWork';

const root: SxProps = { height: `100%`, backgroundColor: "mainContent.main" };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = TaskClient.useComposer();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  const entity = active ? session.getEntity(active.id) : undefined;
  console.log("Opening Route", active?.id);
      
  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!active) {
      return null;
    }
    if (active.id === 'activities') {
      return (<Box sx={root}><Activities /></Box>);
    } 
    if (active.id === 'tasks') {
      return (<Box sx={root}><Tasks /></Box>);  
    } else if (active.id === 'mytasks') {
      return (<Box sx={root}><MyWork /></Box>);  
    }
    if (entity) {
      return <Box sx={root}>entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


