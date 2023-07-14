import React from 'react';
import { Grid, TextField, Box } from '@mui/material';

import { useIntl } from 'react-intl';
import Burger from '@the-wrench-io/react-burger';



interface CustomDialogProps {
  title: string;
  children: React.ReactNode;
  onSave: () => void;

}

const DialogContent: React.FC<{ children: React.ReactNode }> = () => {
  return (
    <></>
  )
}

const EditTaskDialog: React.FC<CustomDialogProps> = (props) => {
  const intl = useIntl();
  const [open, setOpen] = React.useState(false);


  return (

    <>
      <Burger.PrimaryButton label='OPEN DIALOG' onClick={() => setOpen(true)} />

      <Burger.Dialog open={open} onClose={() => setOpen(false)} backgroundColor={'uiElements.main'} 
        title={intl.formatMessage({ id: props.title })}
        children={<DialogContent children={props.children} />}
        actions={<Burger.PrimaryButton label='buttons.apply' onClick={props.onSave} />} />
    </>
  )
}


export default EditTaskDialog;