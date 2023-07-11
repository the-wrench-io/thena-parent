import React from 'react';
import Burger from '@the-wrench-io/react-burger';

//TODO
const DialogContent: React.FC = () => {
  return (
    <>
      CONTENT
    </>
  )
}

const CustomDialog: React.FC = () => {
  const [open, setOpen] = React.useState(false);

  return (

    <>
      <Burger.Dialog open={open}onClose={() => setOpen(false)} title='tasks.createNew'  backgroundColor={'uiElements.main'} 
        children={<DialogContent />} 
        actions={<Burger.PrimaryButton label='buttons.apply' onClick={() => { }} />} />
      <Burger.PrimaryButton label='buttons.apply' onClick={() => setOpen(true)} />
    </>)
}


export default CustomDialog;