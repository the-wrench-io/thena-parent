import React from 'react';
import { TableCell, TableRow, TableCellProps, styled, SxProps, TableBody, Box } from '@mui/material';

import client from '@taskclient';

import * as Cells from './TasksTableCells';
import { SpotLightProgress } from './table-types';

const lineHeight = 28;


const StyledTableBody = styled(TableBody)`
  ${({ theme }) => `
    box-shadow: ${theme.shadows[1]};
    border-top: 2px solid transparent;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-bottom: 8px solid transparent;
    border-radius: 0px 0px 8px 8px;
    background-color: ${theme.palette.background.paper};

    & tr:last-child {
      border-radius: 0px 0px 8px 8px;
    }
        
    & tr:last-child td:first-child {
      border-radius: 0px 0px 0px 8px;
    }
    & tr:last-child td:last-child {
      border-radius: 0px 0px 8px 0px;
    }
  `};
`;

const Cell = styled(TableCell)<TableCellProps>(({ theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  lineHeight: lineHeight + 'px',

  paddingLeft: theme.spacing(2),
  paddingRight: theme.spacing(2),
  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));

function getStatus(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  if (def.type === 'status') {
    const backgroundColor = def.color;
    return { backgroundColor, borderWidth: 0, color: 'primary.contrastText' }
  }
  return undefined;
}

function getPriority(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  if (def.type === 'priority') {
    const backgroundColor = def.color;
    return { backgroundColor, borderWidth: 0, color: 'primary.contrastText' }
  }
  return undefined;
}

function getRoles(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }

  return undefined;
}

function getOwners(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  return undefined;
}


const DescriptorTableRow: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <Cell width="300px"><Cells.Subject maxWidth="300px" row={row} def={def}/></Cell>
    <Cell width="150px" sx={getPriority(def)}><Cells.Priority row={row} def={def}/></Cell>
    <Cell width="200px" sx={getStatus(def)}><Cells.Status row={row} def={def}/></Cell>
    <Cell width="150px" sx={getOwners(def)}><Cells.Owners row={row} def={def}/></Cell>
    <Cell sx={getRoles(def)}><Cells.Roles row={row} def={def}/></Cell>
    <Cell><Cells.DueDate row={row} def={def}/></Cell>
  </TableRow>);
}

const EmptyTableRow: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  def: client.Group,
  loading: boolean
}> = ({ content, def, loading }) => {
  if (content.emptyRows === 0) {
    return null;
  }

  const rows: React.ReactNode[] = [];
  for (let index = 0; index < content.emptyRows; index++) {
    rows.push(<TableRow key={index}>
      <Cell>&nbsp;</Cell>
      <Cell colSpan={5}>{loading ? <SpotLightProgress def={def} /> : null}</Cell>
    </TableRow>)
  }
  return (<>{rows}</>);
}

const Rows: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  def: client.Group,
  loading: boolean
}> = ({ content, def, loading }) => {
  return (
    <StyledTableBody>
      {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} def={def} />))}
      <EmptyTableRow content={content} def={def} loading={loading} />
    </StyledTableBody>)
}

export default Rows;

