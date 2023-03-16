import React from 'react';
import { TableCell, TableRow, TableCellProps, styled, paperClasses } from '@mui/material';

import client from '@taskclient';

import * as Cells from './TasksTableCells';

const lineHeight = 28;
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

const DescriptorTableRow: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  assocs: client.TaskDescriptors
}> = ({ rowId, row, assocs }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <Cell><Cells.Subject row={row} assocs={assocs} /></Cell>
    <Cell><Cells.Priority row={row} assocs={assocs} /></Cell>
    <Cell><Cells.Status row={row} assocs={assocs} /></Cell>
    <Cell><Cells.Owners row={row} assocs={assocs} /></Cell>
    <Cell><Cells.Roles row={row} assocs={assocs} /></Cell>
    <Cell><Cells.DueDate row={row} assocs={assocs} /></Cell>
  </TableRow>);
}

const EmptyTableRow: React.FC<{ content: client.TablePagination<client.TaskDescriptor> }> = ({ content }) => {
  console.log("empty rows", content.emptyRows, "entries", content.entries.length);

  return content.emptyRows > 0 ? <TableRow style={{ height: (lineHeight + 1) * content.emptyRows }}><TableCell colSpan={6} /></TableRow> : null;
}

export { DescriptorTableRow, EmptyTableRow };

