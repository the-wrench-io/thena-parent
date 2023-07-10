import React from 'react';
import { Typography, Stack } from '@mui/material';
import Help from './Help';
import Burger from '@the-wrench-io/react-burger';


const Buttons: React.FC = () => {

  return (

    <>
      <Typography sx={{ mb: 1 }}>Burger buttons</Typography>
      <Stack direction="row" spacing={2} sx={{ mb: 1 }}>
        <Burger.SecondaryButton label='buttons.cancel' onClick={() => { }} />
        <Burger.PrimaryButton label='buttons.apply' onClick={() => { }} />
      </Stack>

      <Typography sx={{ mb: 1 }}>Burger buttons with help</Typography>
      <Stack direction="row" spacing={2} sx={{ mb: 1 }}>
        <Help children={<Burger.SecondaryButton label='buttons.cancel' onClick={() => { }} />} />
        <Help children={<Burger.PrimaryButton label='buttons.apply' onClick={() => { }} />} />
      </Stack>


    </>)
}


export default Buttons;