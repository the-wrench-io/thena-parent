import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import Styles from '@styles';
import client from '@taskclient';
import TasksTable from '../TaskTable';


const Tasks: React.FC<{}> = () => {
  const backend = client.useService();
  const [content, setContent] = React.useState<client.Task[]>();

  React.useEffect(() => {
    if (content == null) {
      backend.active().then(setContent)
    }

  }, [content, setContent]);

  return (<>
    <Box sx={{p: 4}}>
      <TasksTable def={content ? content : []} />
    </Box>
  </>);
}

export { Tasks };
