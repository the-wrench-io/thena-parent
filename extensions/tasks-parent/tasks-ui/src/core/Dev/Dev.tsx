import React from 'react';

import { Box, Divider, Button } from '@mui/material';
import TaskOps from '../TaskOps';

const Dev: React.FC = () => {

const [open, setOpen] = React.useState(false);

  return (
    <Box sx={{ width: '100%', p: 1 }}>
      <Box>COMPONENT 1 create task preview</Box>
      <TaskOps.CreateTaskView />
      <Divider />

      <Divider />
      <Box>Fullscreen Dialog</Box>
      <Button variant='contained' onClick={() => setOpen(true)}>Open dialog</Button>
      <TaskOps.FullscreenDialog backgroundColor='uiElements.main' children={<>djrlaurluar</>} onClose={() => setOpen(false)} open={open} title='Fullscreen dialog'/>

    </Box>);
}

export { Dev };
