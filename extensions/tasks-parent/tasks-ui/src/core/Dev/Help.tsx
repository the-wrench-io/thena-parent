import React from 'react';
import { Box, Typography, IconButton, useTheme } from '@mui/material';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import Burger from '@the-wrench-io/react-burger';



interface HelpProps {
  children: React.ReactNode;
}

const HelpContent: React.FC<{}> = () => {
  return (
    <>
      <Typography variant='body2' sx={{ mb: 1 }}>Here you can find some useful info about this feature you're wondering about</Typography>
      <Typography variant='body1'>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. In pellentesque tortor eleifend congue lobortis. Quisque dictum urna lacinia,
        consequat nulla sed, imperdiet quam. Nunc rhoncus odio sed facilisis egestas. Nulla facilisi. Nullam non ipsum placerat, iaculis arcu non, auctor urna.
        Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nunc vulputate est et urna facilisis, eu euismod massa elementum.
        Etiam quis ultricies leo, vel tempus purus. Sed facilisis vulputate ipsum, ut congue felis malesuada ac. Pellentesque sodales ipsum nibh, et gravida velit placerat eu.
        Phasellus semper diam a libero eleifend, non mattis elit maximus. Aliquam vestibulum velit at blandit tempor. Integer aliquet dignissim mi eget convallis.
        Fusce consectetur eleifend dui, ac molestie nunc luctus id. Proin quis volutpat dui, ut porta felis. Nam porta ligula sem, quis bibendum lorem varius non.
      </Typography>
    </>)
}


const Help: React.FC<HelpProps> = (props) => {
  const theme = useTheme();
  const [open, setOpen] = React.useState(false);


  return (<>
    <Burger.Dialog open={open} onClose={() => setOpen(false)} title='Help topic name' children={<HelpContent />} backgroundColor={'uiElements.main'} />
    <Box display='flex'>
      {props.children}
      <IconButton onClick={() => setOpen(true)}><HelpOutlineIcon fontSize='small' sx={{ color: theme.palette.uiElements.main }} /></IconButton>
    </Box>
  </>
  )
}


export default Help;