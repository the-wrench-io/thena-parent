import React from 'react';
import { TableRow, LinearProgress, Box } from '@mui/material';

import client from '@taskclient';

import * as Cells from './MyWorkTableCells';




const DescriptorTableRow: React.FC<{
  rowId: number,
  row: client.TaskDescriptor
}> = ({ row }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <client.Styles.TableCell width="50px"><Cells.Tools maxWidth="100px" row={row}/></client.Styles.TableCell>
    <client.Styles.TableCell width="300px"><Cells.Subject maxWidth="300px" row={row}/></client.Styles.TableCell>
    <client.Styles.TableCell width="450px"><Cells.Desc maxWidth="450px" row={row} /></client.Styles.TableCell>
    <client.Styles.TableCell rowType={row.uploads.length === 0 ? undefined : 'large'}><Cells.DueDate row={row}/></client.Styles.TableCell>
  </TableRow>);
}



const Rows: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  loading: boolean
}> = ({ content, loading }) => {
  return (
    <client.Styles.TableBody>
      {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} />))}
      <client.Styles.TableRowEmpty content={content} loading={loading} plusColSpan={3} />
    </client.Styles.TableBody>)
}


export default Rows;

