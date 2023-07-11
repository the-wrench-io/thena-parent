# Tasks UI

## Development guidelines

### Dev "space"

#### Description
* Dev is a place for testing components.  Before actually implementing components in the “real” codebase, they should be implemented and tested in Dev first.  
* The intention is that we can catch bugs in Dev before they spread into the live code.
* If needed, multiple Dev-s can be created, such as Dev1, Dev2, etc.  

### Usage

* Declare components in their appropriate folders.
* Import and implement them in Dev.
* **Do not declare components within Dev.**

See example: 


```typescript
import TaskOps from '../TaskOps';

const Dev: React.FC = () => {

  return (
    <>
       <Box>COMPONENT 1 create task preview</Box>
       <TaskOps.CreateTaskView />       
    </>);
}
```

### Naming conventions
TODO

### Styled components
TODO
