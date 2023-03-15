import React from 'react';


import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import EmptyTableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';


import client from '@taskclient';

import { Provider } from './table-ctx';
import TableHeader from './TasksTableHeader';
import TableRow from './TasksTableRow';



const DescriptorTable: React.FC<{ def: client.Task[] }> = ({ def }) => {
  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({ 
    src: new client.TaskDescriptorsImpl(def, undefined).findAll(),
    orderBy: 'created',
    sorted: false }));

  return (<Provider>
    <Paper sx={{ width: '100%', mb: 2 }}>
      <TableContainer>
        <Table size='small'>
          <TableHead><TableHeader content={content} setContent={setContent} /></TableHead>
          <TableBody>
            {content.entries.map((row) => (<TableRow key={row.id} row={row} assocs={assocs} def={def} />))}
            {content.emptyRows > 0 ? <EmptyTableRow style={{ height: (28 + 1) * content.emptyRows }}><TableCell colSpan={6} /></EmptyTableRow> : null}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={content.rowsPerPageOptions}
        component="div"
        count={def.length}
        rowsPerPage={content.rowsPerPage}
        page={content.page}
        onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
        onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
      />
    </Paper>
  </Provider>
  );
}

export default DescriptorTable;

