import React from 'react';
import { Box, AppBar, Toolbar, Typography } from '@mui/material';

import client from '@taskclient';


const Tools: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (<>
    <Box sx={{position: 'fixed'}}>
      <Typography variant="h6" component="div">
        Scroll to see button
      </Typography>
    </Box>

    <Box sx={{pt: 4}}></Box>

    <Box>
      {children}
    </Box>
    
    <Box sx={{pt: 80}}></Box>
  </>);
}

export { Tools };
