import React from 'react';
import { FormGroup, FormControlLabel, Checkbox, Typography, Grid } from '@mui/material';




const StyledCheckList: React.FC<{}> = () => {
  
  return (<Grid item>
    <Typography>Checklist</Typography>
    <FormGroup>
      <FormControlLabel control={<Checkbox defaultChecked />} label="TODO1" />
      <FormControlLabel control={<Checkbox />} label="TODO2" />
      <FormControlLabel control={<Checkbox />} label="TODO3" />
    </FormGroup>
  </Grid>
  )
}

export default StyledCheckList;