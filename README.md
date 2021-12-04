# zeus

A implementation of Shadowsocks in Java base on netty4 framework.

# Modules

There are three modules.

* `zeus-server`: proxy server application
* `zeus-client`: proxy client application
* `zeus-log-analysis` proxy log analysis application.Flume was used to listen on Zeus' log files and send data to Kafka and log analysis by Storm

## Features

proxy tcp data streams

## Development

* IDE: IDEA
* Java Version: JDK1.8
* Maven :3.6.3

## Build & Run

Run `mvn clean install` in the root directory. If you want to run server node:

```
java -jar zeus-server/target/zeus-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You can use `-c file` to specify a json file from your disk. There is a example config.json file.

```
{
  "server": "0.0.0.0",
  "port_password": {
    "9000": "889900"
  },
  "method": "aes-256-cfb",
  "obfs": "origin"
}
```

If you want to run client node:

```
java -jar zeus-client/target/zeus-client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You can use `-c file` to specify a json file from your disk. There is a example config.json file.

```
{
  "server": "127.0.0.1",
  "port_password": {
    "9000": "889900"
  },
  "method": "aes-256-cfb",
  "obfs": "origin",
  "local_address": "127.0.0.1",
  "local_port": 10080
}
```

## Supported Ciphers

### AEAD Ciphers

* `Aes-128-cfb`
* `Aes-192-cfb`
* `Aes-256-cfb`
* `Chacha20`

## TODO

- [ ] Support UDP proxy 