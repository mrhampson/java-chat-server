# java-chat-server
Simple text chat server over TCP implemented in Java.

# Current commands supported

## NICK
```
NICK mrhampson
Aug 17, 2018 1:53:14 AM (mrhampson): mrhampson joined
```
This sets your username in the chat.

## SEND or s
```
SEND Hello!
Aug 17, 2018 1:53:19 AM (mrhampson): Hello!

s Hello!
Aug 17, 2018 1:53:19 AM (mrhampson): Hello!
```
Sends your message to everyone. Currently there are no channels and all clients receive every message sent

# How to connect
## Use `netcat` as a client`
Just run the server and connect by doing `netcat <server ip> 1234`

# How to run the server
Use gradle and run the `runServer` task. This starts the server on port 1234 by default.
