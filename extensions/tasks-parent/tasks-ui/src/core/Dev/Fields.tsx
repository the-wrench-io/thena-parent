import React from 'react';
import Burger from '@the-wrench-io/react-burger';


interface FieldProps {
  label: string,
  helperText?: string,
  required?: boolean,
}

export const DateField: React.FC<FieldProps> = (props) => {
  const [value, setValue] = React.useState('2023-09-19');

  return (
    <>
      <Burger.DateField label={props.label} value={value} helperText={props.helperText} onChange={(newValue) => setValue(newValue)} />
    </>
  )
}

export const TextField: React.FC<FieldProps> = (props) => {
  const [value, setValue] = React.useState('');

  return (
    <>
      <Burger.TextField required={props.required} label={props.label} value={value} helperText={props.helperText} onChange={(newValue) => setValue(newValue)} />
    </>
  )
}





