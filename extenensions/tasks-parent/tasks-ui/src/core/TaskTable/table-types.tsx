import client from '@taskclient';
import { LinearProgress, Box } from '@mui/material';

type SpotLight = SpotLightStatus | SpotLightPriority | SpotLightOwner | SpotLightRole;

interface SpotLightOwner {
  owner: string[];
  type: 'owner';
}

interface SpotLightRole {
  role: string[];
  type: 'role';
}


interface SpotLightStatus {
  status: client.TaskStatus;
  type: 'status'
}

interface SpotLightPriority {
  priority: client.TaskPriority;
  type: 'priority'
}


// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
const bittersweet: string = '#FF595E';
const emerald: string = '#26C485';
const sunglow: string = '#FFCA3A';
const steelblue: string = '#1982C4';
const ultraviolet: string = '#6A4C93';

const priority: Record<client.TaskPriority, string> = {
  'HIGH': bittersweet,
  'LOW': steelblue,
  'MEDIUM': emerald
}

const status: Record<client.TaskStatus, string> = {
  'REJECTED': bittersweet,
  'IN_PROGRESS': emerald,
  'COMPLETED': steelblue,
  'CREATED': ultraviolet,
}

const SpotLightColors = {
  priority,
  status,
  pallette: { bittersweet, emerald, sunglow, steelblue, ultraviolet }
};

const SpotLightProgress: React.FC<{ value: SpotLight | undefined }> = ({ value }) => {

  return (<Box sx={{ width: '100%' }}>
    <LinearProgress color='primary' />
  </Box>);
}
export { SpotLightProgress, SpotLightColors };
export type { SpotLight, SpotLightRole, SpotLightPriority };


