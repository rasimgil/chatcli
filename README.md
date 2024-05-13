# Client-Server Chatroom
A client-server chatroom application that supports multiple clients.
## Requirements
1. Java (JDK 8+)
## Building the project
1. **Clone the repository**
    ```bash
    git clone https://github.com/rasimgil/chatcli
    cd chatcli
    ```
2. **Build the project**
    ```bash
    mvn clean install 
    ```
## Setup
### Starting the server
To start the server, run
```bash
java -cp target/chatcli-1.0-SNAPSHOT.jar server.Server <port>
```
- Replace `<port>` with the desired port number to listen for incoming connections.
### Connecting to the server
To connect a client to the server, run
```bash
java -cp target/chatcli-1.0-SNAPSHOT.jar client.Client <server_ip> <server_port>
```
- Replace `<server_ip>` with the IP address of the server, 
and `<server_port>` with the port number on which the server is running.
## Usage
1. Upon connecting to the server, the user will be prompted to enter a username.
   - the username has to be a unique string consisting of only alphanumerical characters.
2. After user is assigned a username, they are added to the default chatroom `Main`.
3. User can send messages to the current chatroom by typing and pressing Enter.
4. Users can execute commands prefixed with "\\" to perform actions.
## Available commands
- `\\list [room_name]`: Lists all available rooms, or a specific room if the room is passed to the command.
- `\\create <room_name>`: Creates a new chatroom.
- `\\join <room_name>`: Joins an existing chatroom.

(As it is for usernames, the room name also has to be a unique string consisting of only alphanumerical characters) 
- `\\shout <message>`: Broadcasts a message to all users connected to the server.