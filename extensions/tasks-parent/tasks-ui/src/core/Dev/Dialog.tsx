import React from 'react';
import { Typography } from '@mui/material';
import { TextField, DateField } from './Fields';
import Burger from '@the-wrench-io/react-burger';


const DialogContent: React.FC = () => {
  return (
    <>
      <TextField label='tasks.title' helperText='tasks.createNew.helperText' required />
      <DateField label='tasks.dueDate' helperText='tasks.dueDate.desc' required />
    </>
  )
}

const Dialog: React.FC = () => {
  const [open, setOpen] = React.useState(false);

  return (

    <>
      <Burger.Dialog open={open} onClose={() => setOpen(false)} title='tasks.createNew' children={<DialogContent />} backgroundColor={'uiElements.main'} />
      <Typography sx={{ mb: 1 }}>Dialog</Typography>
      <Burger.PrimaryButton label='buttons.apply' onClick={() => setOpen(true)} />
    </>)
}


export default Dialog;