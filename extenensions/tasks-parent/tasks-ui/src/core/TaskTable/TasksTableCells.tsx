import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import client from '@taskclient';

import { useTable } from './table-ctx';
import { TasksTableCell } from './TasksTableCell';

interface CellProps {
  row: client.TaskDescriptor,
  assocs: client.TaskDescriptors
  width: string,
}
const Desc: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Desc"} width={width} name={row.description} />);
}
const DueDate: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/DueDate"} width={width} name={row.dueDate + ""} />);
}
const Status: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Status"} width={width} name={row.status} />);
}
const Roles: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Roles"} width={width} name={row.roles.join(", ")} />);
}
const Owners: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Owners"} width={width} name={row.owners.join(", ")} />);
}
const Priority: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Priority"} width={width} name={row.priority} />);
}
const Subject: React.FC<CellProps> = ({ row, width }) => {
  return (<TasksTableCell id={row.id + "/Subject"} width={width} name={row.subject} />);
}

export type { CellProps }
export { Subject, Priority, Status, Owners, DueDate, Roles, Desc };

