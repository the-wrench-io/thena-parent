import React from 'react';

import { Box, TablePagination, TableContainer, Table } from '@mui/material';

import Burger from '@the-wrench-io/react-burger';
import client from '@taskclient';

import { SpotLight } from './table-types';
import { Provider } from './table-ctx';
import TableHeader from './TasksTableHeader';
import TableRows from './TasksTableRow';


const DescriptorTable: React.FC<{ def?: client.Task[], spotLight?: SpotLight }> = ({ def, spotLight }) => {
  const drawer = Burger.useDrawer();
  const drawerOpen = drawer.session.drawer

  const [assocs, setAssocs] = React.useState(new client.TaskDescriptorsImpl(def ? def : [], undefined))
  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({
    src: assocs.findAll(),
    orderBy: 'created',
    sorted: false
  }));

  React.useEffect(() => {
    setAssocs(prev => {
      console.log("Tasks Table Reloading")
      const next = prev.withValues(def ? def : [])
      setContent(c => c.withSrc(next.findAll()));
      return next;
    });
  }, [def, setContent, setAssocs])

  const loading: boolean = def ? false : true;

  return (<Provider>
    <Box sx={{ width: '100%' }}>
      <TableContainer>
        <Table size='small'>
          <TableHeader content={content} setContent={setContent} spotLight={spotLight} />
          <TableRows content={content} spotLight={spotLight} assocs={assocs} loading={loading} />
        </Table>
      </TableContainer>
      <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
        <Box alignSelf="center" flexGrow={1}>Grouped by</Box> {
          !def ? null :
            (<TablePagination
              rowsPerPageOptions={content.rowsPerPageOptions}
              component="div"
              count={(def as any).length}
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

