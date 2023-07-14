import { CreateTaskView } from './CreateTaskView';
import EditTaskDialog from './EditTaskDialog';
import { FullscreenDialog as FullscreenDialogAs }  from './FullscreenDialog';

const ChangeTaskView = CreateTaskView;
const ChangeTasksView = CreateTaskView;
const ArchiveTaskView = CreateTaskView;
const FullscreenDialog = FullscreenDialogAs;
const EditTask = EditTaskDialog;



const result = { CreateTaskView, ChangeTaskView, ArchiveTaskView, ChangeTasksView, EditTask, FullscreenDialog };

export default result;