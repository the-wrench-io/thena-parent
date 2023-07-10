import React from 'react';

import { Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Buttons from './Buttons';
import Dialog from './Dialog';
import { DateField, TextField } from './Fields';


const Dev: React.FC = () => {


  return (
    <Box sx={{ width: '100%', p: 1 }}>
      <Buttons />
      <Box sx={{ m: 1 }} />
      <Dialog />
      <Box sx={{ m: 1 }} />
      <Typography><FormattedMessage id='fields.dueDate' /></Typography>
      <DateField label='fields.dueDate'/>
      <Box sx={{ m: 1 }} />
      <Typography><FormattedMessage id='fields.textField' /></Typography>
      <TextField label='fields.textField' />

    </Box>);
}

export { Dev };
