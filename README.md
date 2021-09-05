# Tempremote
Java command line program for reading temperature and 
sending to a server

## Build
To Build using maven:

`mvn clean compile assembly:single`

### Protobuf support
This project uses protobuf, a valid protobuf compiler must be available for
 the above maven command to be available. For more information see [compile protobuf with maven](https://dzone.com/articles/compile-protocol-buffers-using-maven) 

## Properties file
Properties can be specified in a json:

```
{
    "remote_id":<id from server>,
    "server_address": <server address>,
    "read_freq": <how often to measure temperature in seconds>
}
```

