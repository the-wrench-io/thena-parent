import React from 'react';
import { Box, Button, TableHead, TableCell, TableRow, TableSortLabel } from '@mui/material';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage, useIntl } from 'react-intl';

import client from '@taskclient';

interface HeadCell {
  id: keyof client.TaskDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'priority' },
  { id: 'status' },
  { id: 'owners' },
  { id: 'roles' },
  { id: 'dueDate' },
  //{ id: 'subject' },
];



const SortableHeader: React.FC<{
  id: keyof client.TaskDescriptor,
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof client.TaskDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>

      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`tasktable.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}

const StyledSpotLight: React.FC<{ value: client.Group }> = ({ value }) => {
  const intl = useIntl();
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset" };
  if (!value) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (value.type === 'status') {
    const backgroundColor = value.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.status.${value.id}`} />
    </Button>);
  } else if (value.type === 'priority') {
    const backgroundColor = value.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.priority.${value.id}`} />
    </Button>);
  } else if (value.type === 'owners' || value.type === 'roles') {
    const backgroundColor = value.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      {value.id === client._nobody_ ? intl.formatMessage({ id: value.id }) : value.id}
    </Button>);
  }
  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`tasktable.header.spotlight.no_group`} />
  </Button>);
}

const DescriptorTableHeader: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>,
  def: client.Group
}> = ({ content, setContent, def }) => {

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'>
          <StyledSpotLight value={def} />
        </TableCell>
        {headCells.map((headCell) => (<SortableHeader key={headCell.id} id={headCell.id} content={content} setContent={setContent} />))}
      </TableRow>
    </TableHead>
  );
}
//border-top-right-radius
export default DescriptorTableHeader;



