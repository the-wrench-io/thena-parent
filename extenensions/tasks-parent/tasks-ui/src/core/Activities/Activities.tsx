import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import Styles from '@styles';


type ActivityId = "tasks";


const createActivities: (props: {
  actions: Burger.TabsActions,
  setOpen: (index: ActivityId) => void
}) => Styles.StyledCardItemProps[] = ({ actions, setOpen }) => ([

  { id: "tasks", title: "activities.tasks.title",
    content: {
      label: "activities.tasks.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({id: 'tasks', label: "tasks"})
    }
  },
]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const [open, setOpen] = React.useState<ActivityId>();
  const cards = React.useMemo(() => createActivities({ actions, setOpen }), [actions, setOpen]);
  const handleClose = () => setOpen(undefined)


  return (<>
    <Styles.Cards title="activities.title" desc="activities.desc" items={cards} />
  </>);
}

export { Activities };
