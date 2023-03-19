import React from 'react';
import { Box } from '@mui/material';

import client from '@taskclient';
import TasksTable from '../TaskTable';
import Tools from '../Tools';


const Tasks: React.FC<{}> = () => {
  const tasks = client.useTasks();


  return (<Tools><>
    {tasks.state.groups.map((group, index) => (<React.Fragment key={group.id}>
      {index > 0 ? <Box sx={{ p: 2 }} /> : null}
      <TasksTable def={group} />
    </React.Fragment>))}
  </>
  </Tools>);
}

export { Tasks };
