import React from 'react';


import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';

import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import Paper from '@mui/material/Paper';


import client from '@taskclient';

import { Provider } from './table-ctx';
import TableHeader from './TasksTableHeader';
import {DescriptorTableRow, EmptyTableRow } from './TasksTableRow';



const DescriptorTable: React.FC<{ def: client.Task[] }> = ({ def }) => {
  const [assocs, setAssocs] = React.useState(new client.TaskDescriptorsImpl(def, undefined))
  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({ 
    src: assocs.findAll(),
    orderBy: 'created',
    sorted: false }));

  return (<Provider>
    <Paper sx={{ width: '100%', mb: 2 }}>
      <TableContainer>
        <Table size='small'>
          <TableHead><TableHeader content={content} setContent={setContent} /></TableHead>
          <TableBody>
            {content.entries.map((row) => (<DescriptorTableRow key={row.id} row={row} assocs={assocs} />))}
            <EmptyTableRow content={content} />
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

