import React from 'react';
import { Box, Button, TableHead, TablePagination } from '@mui/material';
import TableCell from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import client from '@taskclient';
import { SpotLight, SpotLightColors } from './table-types';

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

const StyledSpotLight: React.FC<{ value: SpotLight | undefined }> = ({ value }) => {

  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset" };
  if (!value) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (value.type === 'status') {
    const backgroundColor = SpotLightColors.status[value.status];
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.status.${value.status}`} />
    </Button>);
  }
  return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
}

const DescriptorTableHeader: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>,
  spotLight: SpotLight | undefined
}> = ({ content, setContent, spotLight }) => {

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none' width="300px">
          <StyledSpotLight value={spotLight} />
        </TableCell>
        {headCells.map((headCell) => (<SortableHeader id={headCell.id} content={content} setContent={setContent} />))}
      </TableRow>
    </TableHead>
  );
}
//border-top-right-radius
export default DescriptorTableHeader;



