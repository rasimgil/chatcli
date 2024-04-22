# Core features 
1. The project will be a client-server based chatting application on the command line interface. 
2. There will be an application for launching a server, the chatroom will be hosted on this server.
3. Clients will launch the client application and connect to the server, which address will be passed as a command line argument. 
4. When connecting to the server the user can choose their custom identifier. If they wish not to, they will be assigned a default name - something like user<id_number>. 
5. After connecting to the server, the client can choose to create a new room with a unique custom identifier, connect to a room if they already know the room's identifier, or ask the application to list all rooms. 
6. There will be a general chatroom in the application. The users can broadcast to the general chatroom by using a custom tag @general before the message