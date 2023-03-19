import client from '@taskclient';
import { LinearProgress, Box } from '@mui/material';




const SpotLightProgress: React.FC<{ def: client.Group }> = ({ def }) => {
  return (<Box sx={{ width: '100%' }}>
    <LinearProgress color='primary' />
  </Box>);
}
export { SpotLightProgress };


