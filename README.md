#Tempremote
Java command line program for reading temperature and 
sending to a server

##Build
To Build using maven:

`mvn clean compile assembly:single`

##Properties file
Properties can be specified in a json:

```
{
    "remote_id":<id from server>,
    "server_address": <server address>,
    "read_freq": <how often to measure temperature in seconds>
}
```

