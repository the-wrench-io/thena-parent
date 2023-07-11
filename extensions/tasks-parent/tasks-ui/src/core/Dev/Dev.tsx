import React from 'react';

import { Box, Divider } from '@mui/material';
import TaskOps from '../TaskOps';


const Dev: React.FC = () => {

  return (
    <Box sx={{ width: '100%', p: 1 }}>
    
      <Divider />
        <Box>COMPONENT 1 create task preview</Box>
        <TaskOps.CreateTaskView />
      <Divider />
      
      
    </Box>);
}

export { Dev };
