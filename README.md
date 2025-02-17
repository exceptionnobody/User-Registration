# User Registration

The prune expired data job is executed every 1 minute.
To change that modify in ```application.properties``` the fields:
1. ```job.execution.rate```
2. ```job.initial.delay```

## Improvements

- Docker file
- Fix bug in the EmailService
- Adding a unit test for the schedule