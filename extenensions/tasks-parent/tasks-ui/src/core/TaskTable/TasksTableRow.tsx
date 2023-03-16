import React from 'react';
import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';

import client from '@taskclient';

import * as Cells from './TasksTableCells';

const lineHeight = 28;
const StyledTableCell = styled(TableCell)<TableCellProps>(({ theme }) => ({
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
  row: client.TaskDescriptor,
  assocs: client.TaskDescriptors
}> = ({ row, assocs }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell><Cells.Subject  width="200px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.Priority width="300px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.Status   width="300px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.Owners   width="150px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.DueDate  width="150px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.Roles    width="150px" row={row} assocs={assocs} /></StyledTableCell>
    <StyledTableCell><Cells.Desc     width="150px" row={row} assocs={assocs} /></StyledTableCell>
  </TableRow>);
}

const EmptyTableRow: React.FC<{ content: client.TablePagination<client.TaskDescriptor> }> = ({ content }) => {
  return content.emptyRows > 0 ? <TableRow style={{ height: (lineHeight + 1) * content.emptyRows }}><TableCell colSpan={6} /></TableRow> : null;
}

export { DescriptorTableRow, EmptyTableRow };

