Tools for building resilient Web applications
=============================================

# Description

This repository contains server-side and client-side libraries for writing resilient Web applications (namely applications that support offline mode):

- a Scala library handling client/server synchronization from the server-side,
- a Scala library to store an event log in a MongoDB database,
- a JavaScript library handling client/server synchronization from the client-side,
- a JavaScript library for making HTTP requests.

# Run the Samples

Run sbt, select the sample project you want to test (`sample-todo` or `sample-notes`) and run it:

```bash
$ sbt
> project sample-todo
[sample-todo] $ run
```