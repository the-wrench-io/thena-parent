import React from 'react';
import { Box } from '@mui/material';

import client from '@taskclient';
import TasksTable from '../TaskTable';
import Tools from '../Tools';


const Tasks: React.FC<{}> = () => {
  const backend = client.useService();
  const [content, setContent] = React.useState<client.Task[]>();

  React.useEffect(() => {
    if (content == null) {
      backend.active().then(setContent)
    }

  }, [content, setContent]);

  return (<Tools>
    <>
      <TasksTable def={content} spotLight={{ type: 'status', status: 'CREATED' }} />
      <Box sx={{ p: 2 }} />
      <TasksTable def={content} spotLight={{ type: 'status', status: 'IN_PROGRESS' }} />
      <Box sx={{ p: 2 }} />
      <TasksTable def={content} spotLight={{ type: 'status', status: 'COMPLETED' }} />
    </>
  </Tools>);
}

export { Tasks };
