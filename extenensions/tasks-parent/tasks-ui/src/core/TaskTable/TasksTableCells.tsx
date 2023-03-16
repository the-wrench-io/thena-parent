import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import client from '@taskclient';

import { useTable } from './table-ctx';
import { TasksTableCell } from './TasksTableCell';

interface CellProps {
  row: client.TaskDescriptor,
  assocs: client.TaskDescriptors
}
const Desc: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Desc"} name={row.description} />);
}
const DueDate: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/DueDate"} name={row.dueDate + ""} />);
}
const Status: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Status"} name={row.status} />);
}
const Roles: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Roles"} name={row.roles.join(", ")} />);
}
const Owners: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Owners"} name={row.owners.join(", ")} />);
}
const Priority: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Priority"} name={row.priority} />);
}
const Subject: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Subject"} name={row.subject} />);
}

export type { CellProps }
export { Subject, Priority, Status, Owners, DueDate, Roles, Desc };

