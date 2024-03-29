import React from 'react';
import { Tab, Box, TabProps, BoxProps, alpha, Tabs, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";
import AppsIcon from '@mui/icons-material/Apps';
import SearchIcon from '@mui/icons-material/Search';
import PieChartIcon from '@mui/icons-material/PieChart';
import HistoryIcon from '@mui/icons-material/History';
import PersonIcon from '@mui/icons-material/Person';
import DashboardIcon from '@mui/icons-material/Dashboard';
import CircleNotificationsIcon from '@mui/icons-material/CircleNotifications';
import { FormattedMessage } from 'react-intl';
import Burger from '@the-wrench-io/react-burger';
import Taskclient from '@taskclient';


const StyledTitleTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem',
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
    backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  },
}));

const StyledBox = styled(Box)<BoxProps>(({ theme }) => ({
  borderBottom: `1px solid ${theme.palette.explorerItem.dark}`,
  width: '100%',
}));



const StyledExplorerTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    minHeight: theme.spacing(5),
    color: theme.palette.explorerItem.main,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    maxWidth: "unset",
    backgroundColor: alpha(theme.palette.explorerItem.dark, 0.2),
  },
  "& .MuiTab-iconWrapper": {
    marginBottom: 'unset',
    marginRight: theme.spacing(2)
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({

  "& .MuiTabs-indicator": {
    backgroundColor: theme.palette.explorerItem.dark,
    width: '3px',
    right: 'unset'
  },
  "& .MuiTabs-flexContainerVertical": {
    "alignItems": 'flex-start',
  }
}));


const Secondary: React.FC<{}> = () => {

  const { actions } = Burger.useTabs();
  const { session } = Taskclient.useComposer();
  const [active, setActive] = React.useState<string>('');

  function handleActive(_event: React.SyntheticEvent, newValue: string) { setActive(newValue) }
  function handleTasks() { actions.handleTabAdd({ id: 'tasks', label: <FormattedMessage id="activities.tasks.title" /> }) }
  function handleGroup() { actions.handleTabAdd({ id: 'group', label: <FormattedMessage id="activities.group.title" /> }) }
  function handleMyTasks() { actions.handleTabAdd({ id: 'mytasks', label: <FormattedMessage id="activities.mytasks.title" /> }) }

  function handleMyHistory() { actions.handleTabAdd({ id: 'myhistory', label: <FormattedMessage id="activities.myhistory.title" /> }) }
  function handleSearch() { actions.handleTabAdd({ id: 'search', label: <FormattedMessage id="activities.search.title" /> }) }
  function handleReporting() { actions.handleTabAdd({ id: 'reporting', label: <FormattedMessage id="activities.reporting.title" /> }) }

  function handleDashboard() { actions.handleTabAdd({ id: 'activities', label: <FormattedMessage id="activities.title" /> }) }

  return (<Box sx={{ backgroundColor: "explorer.main", height: '100%', width: '100%' }}>
    <StyledBox>
      <StyledTitleTab label={<FormattedMessage id="explorer.title" />} value='label' />
    </StyledBox>
    <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", backgroundColor: "explorer.main" }}>
      <StyledTabs orientation="vertical" onChange={handleActive} value={active}>
        <StyledExplorerTab value='' icon={<DashboardIcon />} label={<FormattedMessage id="activities.title" />} onClick={handleDashboard} />
        <StyledExplorerTab value='explorer.search' icon={<SearchIcon />} label={<FormattedMessage id="activities.search.title" />} onClick={handleSearch} />
        <StyledExplorerTab value='explorer.tasks' icon={<AppsIcon />} label={<FormattedMessage id="activities.tasks.title" />} onClick={handleTasks} />
        <StyledExplorerTab value='explorer.group' icon={<CircleNotificationsIcon />} label={<FormattedMessage id="activities.group.title" />} onClick={handleGroup} />
        <StyledExplorerTab value='explorer.mytasks' icon={<PersonIcon />} label={<FormattedMessage id="activities.mytasks.title" />} onClick={handleMyTasks} />
        <StyledExplorerTab value='explorer.myhistory' icon={<HistoryIcon />} label={<FormattedMessage id="activities.myhistory.title" />} onClick={handleMyHistory} />
        <StyledExplorerTab value='explorer.reporting' icon={<PieChartIcon />} label={<FormattedMessage id="activities.reporting.title" />} onClick={handleReporting} />

      </StyledTabs>
    </Box>
  </Box>)
}
export { Secondary }


