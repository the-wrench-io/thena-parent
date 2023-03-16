import React from 'react';

import { Box, styled } from '@mui/material';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';

import Burger from '@the-wrench-io/react-burger';
import client from '@taskclient';

import { Provider } from './table-ctx';
import TableHeader from './TasksTableHeader';
import { DescriptorTableRow, EmptyTableRow } from './TasksTableRow';


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

const StylesTableContainer = styled(TableContainer)`
  ${({ theme }) => `
    
  `};
`;

const DescriptorTable: React.FC<{ def: client.Task[] }> = ({ def }) => {
  const drawer = Burger.useDrawer();
  const drawerOpen = drawer.session.drawer

  const [assocs, setAssocs] = React.useState(new client.TaskDescriptorsImpl(def, undefined))
  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({
    src: assocs.findAll(),
    orderBy: 'created',
    sorted: false
  }));

  React.useEffect(() => {
    setAssocs(prev => {
      console.log("Tasks Table Reloading")
      const next = prev.withValues(def)
      setContent(c => c.withSrc(next.findAll()));
      return next;
    });
  }, [def, setContent, setAssocs])

  return (<Provider>
    <Box>
      <Box sx={{ width: '100%' }}>
        <StylesTableContainer>
          <Table size='small'>
            <TableHead><TableHeader content={content} setContent={setContent} /></TableHead>
            <StyledTableBody>
              {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} assocs={assocs} />))}
              <EmptyTableRow content={content} />
            </StyledTableBody>
          </Table>
        </StylesTableContainer>
      </Box>

      <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
        <Box alignSelf="center" flexGrow={1}>Grouped by</Box>
        <TablePagination
          rowsPerPageOptions={content.rowsPerPageOptions}
          component="div"
          count={def.length}
          rowsPerPage={content.rowsPerPage}
          page={content.page}
          onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
          onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
        />
      </Box>
    </Box>
  </Provider>
  );
}

export default DescriptorTable;

