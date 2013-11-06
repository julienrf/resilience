Tools for building resilient Web applications
=============================================

# Description

This repository contains server-side and client-side libraries for writing resilient Web applications:

- a Scala library handling client/server synchronization from the server-side,
- a Scala library to store an event log in a MongoDB database,
- a JavaScript library handling client/server synchronization from the client-side,
- a JavaScript library for making HTTP requests.

# Run the Samples

First, locally publish the Scala libraries:

```bash
$ cd src/server
$ sbt publish-local
```

Then run a sample:

```bash
$ cd samples/todo
$ sbt run
```