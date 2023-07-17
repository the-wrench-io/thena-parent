import React from 'react';
import { styled } from "@mui/material/styles";
import { Dialog, DialogTitle, DialogContent, DialogActions, Box, Grid } from '@mui/material';
import Burger from '@the-wrench-io/react-burger';
import Styles from '@styles';

const StyledDialogTitle = styled(DialogTitle)(({ theme }) => ({
  fontWeight: 'bold',
  borderBottom: `1px solid ${theme.palette.uiElements.main}`
}));


interface StyledDialogProps {
  headerToolbar: React.ReactNode;
  titleArgs?: {};
  onClose: () => void;
  submit?: {
    title: string;
    disabled: boolean;
    onClick: () => void;
  };
  actions?: React.ReactElement;
  open: boolean;
  children: React.ReactElement;
}


const Left: React.FC<{}> = () => {

  return (
    <Grid item md={6} lg={6} xl={6} sx={{ height: '100vh' }}>
      <Burger.TextField label='task title' onChange={() => { }} value='' required />
      <Burger.TextField label='task description' onChange={() => { }} value='' required />
      <Styles.Checklist />
    </Grid>
  )
}

const Right: React.FC<{}> = () => {
  return (
    <Grid item md={6} lg={6} xl={6} sx={{ height: '100vh' }}>
      RIGHT
    </Grid>
  )
}

const FullscreenDialog: React.FC<StyledDialogProps> = (props) => {

  return (
    <Dialog open={props.open} onClose={props.onClose} fullScreen sx={{ m: 2 }}>
      <StyledDialogTitle>{props.headerToolbar}</StyledDialogTitle>
      <DialogContent>
        <Grid container spacing={1}>
          <Left />
          <Right />
        </Grid>
      </DialogContent>
      <DialogActions>
        <Box display="inline-flex">
          {props.actions}
          <Burger.SecondaryButton sx={{ mr: 1 }} onClick={props.onClose} label="buttons.cancel" />
          {props.submit ? <Burger.PrimaryButton onClick={props.submit.onClick} disabled={props.submit.disabled} label={props.submit.title} /> : undefined}
        </Box>
      </DialogActions>
    </Dialog>
  );
}

export type { StyledDialogProps }
export { FullscreenDialog }