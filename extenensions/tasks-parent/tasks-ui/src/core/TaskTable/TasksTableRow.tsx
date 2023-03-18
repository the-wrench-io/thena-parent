import React from 'react';
import { TableCell, TableRow, TableCellProps, styled, SxProps, TableBody } from '@mui/material';

import client from '@taskclient';

import * as Cells from './TasksTableCells';
import { SpotLight, SpotLightColors, SpotLightProgress } from './table-types';

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

function getStatus(value: SpotLight | undefined): SxProps | undefined {
  if (!value) {
    return undefined;
  }
  if (value.type === 'status') {
    const backgroundColor = SpotLightColors.status[value.status];
    return { backgroundColor }
  }
  return undefined;
}

function getPriority(value: SpotLight | undefined): SxProps | undefined {
  if (!value) {
    return undefined;
  }
  if (value.type === 'priority') {
    const backgroundColor = SpotLightColors.priority[value.priority];
    return { backgroundColor }
  }
  return undefined;
}

function getRoles(value: SpotLight | undefined): SxProps | undefined {
  if (!value) {
    return undefined;
  }

  return undefined;
}

function getOwners(value: SpotLight | undefined): SxProps | undefined {
  if (!value) {
    return undefined;
  }
  return undefined;
}


const DescriptorTableRow: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  assocs: client.TaskDescriptors,
  spotLight: SpotLight | undefined
}> = ({ rowId, row, assocs, spotLight }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <Cell><Cells.Subject row={row} assocs={assocs} /></Cell>
    <Cell sx={getPriority(spotLight)}><Cells.Priority row={row} assocs={assocs} /></Cell>
    <Cell sx={getStatus(spotLight)}><Cells.Status row={row} assocs={assocs} /></Cell>
    <Cell sx={getOwners(spotLight)}><Cells.Owners row={row} assocs={assocs} /></Cell>
    <Cell sx={getRoles(spotLight)}><Cells.Roles row={row} assocs={assocs} /></Cell>
    <Cell><Cells.DueDate row={row} assocs={assocs} /></Cell>
  </TableRow>);
}

const EmptyTableRow: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  spotLight: SpotLight | undefined,
  loading: boolean
}> = ({ content, spotLight, loading }) => {
  if (content.emptyRows === 0) {
    return null;
  }

  const rows: React.ReactNode[] = [];
  for (let index = 0; index < content.emptyRows; index++) {
    rows.push(<TableRow key={index}>
      <Cell>&nbsp;</Cell>
      <Cell colSpan={5}>{loading ? <SpotLightProgress value={spotLight} /> : null}</Cell>
    </TableRow>)
  }
  return (<>{rows}</>);
}

const Rows: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  assocs: client.TaskDescriptors,
  spotLight: SpotLight | undefined,
  loading: boolean
}> = ({ content, spotLight, loading, assocs }) => {
  return (
    <StyledTableBody>
      {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} assocs={assocs} spotLight={spotLight} />))}
      <EmptyTableRow content={content} spotLight={spotLight} loading={loading} />
    </StyledTableBody>)
}

export default Rows;

