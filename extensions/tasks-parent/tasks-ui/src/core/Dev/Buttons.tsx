import React from 'react';
import { Box, Typography, Stack } from '@mui/material';
import Burger from '@the-wrench-io/react-burger';


const Buttons: React.FC<{}> = () => {

  return (

    <Box>
      <Typography sx={{ mb: 1 }}>Burger buttons</Typography>
      <Stack direction="row" spacing={2} sx={{ mb: 1 }}>
        <Burger.PrimaryButton label='PRIMARY' onClick={() => { }} />
        <Burger.SecondaryButton label='SECONDARY' onClick={() => { }} />
      </Stack>
    </Box>)
}


export default Buttons;