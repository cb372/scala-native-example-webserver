# Scala Native example - webserver

Companion code for this blogpost: http://tech.ovoenergy.com/scala-native-webserver/

This is a toy HTTP erver that simply responds to any request with some info about that request.

It is implemented using [libuv](http://libuv.org/) for event-driven I/O.

## To install dependencies

On a Mac:

```
$ brew install llvm bdw-gc re2 libuv
```

For other platforms, see the official Scala Native docs.

## To run

```
$ sbt run
```

## To run the unit tests

```
$ sbt test
```
