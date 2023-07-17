import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';

import * as TaskTableStyles from './StyledTaskTable';

import StyledCheckList, * as Checklist from './StyledChecklist';


declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };

}


namespace Styles {
  export const Cards = StyledCards;
  export const Checklist = StyledCheckList;
  export const TaskTable = { 
    TableCell: TaskTableStyles.StyledTableCell,
    TableBody: TaskTableStyles.StyledTableBody,
    TableRowEmpty: TaskTableStyles.StyledEmptyTableRow,
    TableHeaderSortable: TaskTableStyles.StyledSortableHeader,
    lineHeight: TaskTableStyles.lineHeight,
    lineHeightLarge: TaskTableStyles.lineHeightLarge,
  }
}

export default Styles;


