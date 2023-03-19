import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';

import client from '@taskclient';

import { Provider } from './table-ctx';
import TableHeader from './TasksTableHeader';
import TableRows from './TasksTableRow';


const DescriptorTable: React.FC<{ def: client.Group }> = ({ def }) => {
  const { loading } = client.useTasks();

  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({
    src: def.records,
    orderBy: 'created',
    sorted: false
  }));

  React.useEffect(() => {
    setContent(c => c.withSrc(def.records));
  }, [def, setContent])

  

  return (<Provider>
    <Box sx={{ width: '100%' }}>
      <TableContainer>
        <Table size='small'>
          <TableHeader content={content} setContent={setContent} def={def} />
          <TableRows content={content} def={def} loading={loading} />
        </Table>
      </TableContainer>
      <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
        <Box alignSelf="center" flexGrow={1}>Grouped by</Box> {
          loading ? null :
            (<TablePagination
              rowsPerPageOptions={content.rowsPerPageOptions}
              component="div"
              count={def.records.length}
              rowsPerPage={content.rowsPerPage}
              page={content.page}
              onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
              onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
            />)
        }
      </Box>
    </Box>
  </Provider>
  );
}

export default DescriptorTable;

