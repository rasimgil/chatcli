package server;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the communication with a single client in the chat room.
 */
public class ClientHandler implements Runnable {
    private static int idCounter = 0;
    private final Socket clientSocket;
    private final Server server;
    private final BufferedReader in;
    private final PrintWriter out;

    /**
     * The unique identifier for this client.
     */
    public String id;

    /**
     * The current room of the client.
     */
    public String room;

    private static final Set<ClientHandler> clients = new HashSet<>();
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW_BOLD = "\033[1;33m";
    private static final String ANSI_RED_BOLD = "\033[1;31m";

    /**
     * Constructs a new {@link ClientHandler} object.
     * @param socket The {@link Socket} the client is associated with.
     * @param server The {@link Server} instance.
     */
    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            synchronized (clients) {
                clients.add(this);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client: clients) {
                if (client != this) {
                    client.out.println(message);
                }
            }
        }
    }
    private void broadcastRoom(String message) {
        synchronized (clients) {
            for (ClientHandler client: clients) {
                if (client != this && client.room.equals(this.room)) {
                    client.out.println(message);
                }
            }
        }
    }

    private String userMessage(String message) {
        return ANSI_CYAN + this.room + "[" + this.id + "]: " + message + ANSI_RESET;
    }

    private String shoutMessage(String message) {
        return ANSI_CYAN +  "Main[" + this.id + "]: " + message + ANSI_RESET;
    }

    private String systemMessage(String message) {
        return ANSI_YELLOW_BOLD + "[Server]: " + message + ANSI_RESET;
    }

    private String errorMessage(String message) {
        return ANSI_RED_BOLD + "[Server.Server]: " + message + ANSI_RESET;
    }

    private boolean invalidString(String s) {
        String alphaNumPattern = "[a-zA-Z0-9]*";
        Pattern p = Pattern.compile(alphaNumPattern);
        Matcher matcher = p.matcher(s);
        return !matcher.matches();
    }

    private void handlePeople(String room) {
        var users = server.getUsersInRoom(room);
        if (Objects.equals(users, Set.of("false"))) {
            out.println(errorMessage("Room " + room + " does not exist."));
        } else {
            String people = systemMessage(room + Arrays.toString(users.toArray()));
            out.println(people);
        }
    }

    private void handleJoin(String room) {
        try {
            if (!server.addUserToRoom(id, room)) {
                out.println(errorMessage("Error joining " + room));
            } else {
                String currentRoom = this.room;
                synchronized (clients) {
                    if (!server.removeUserFromRoom(id, currentRoom)) {
                        out.println(errorMessage("Could not leave the room"));
                        return;
                    } else {
                        broadcastRoom(systemMessage(this.id + " left the room."));
                        this.room = room;
                    }
                }
                out.println(systemMessage("Joined " + room));
                synchronized (clients) {
                    broadcastRoom(systemMessage(this.id + " joined the room"));
                }
            }
        } catch (IllegalArgumentException e) {
            out.println(errorMessage(e.getMessage()));
        }
    }

    private void handleCreate(String room) {
        try {
            if (invalidString(room) || "main".equalsIgnoreCase(room)) {
                out.println(errorMessage("Invalid room name."));
            } else {
                if (!server.createRoom(room)) {
                    out.println(errorMessage("Error creating "  + room));
                } else {
                    out.println(systemMessage("Created " + room));
                }
            }
        } catch (RuntimeException e) {
            out.println(errorMessage(e.getMessage()));
        }
    }

    private boolean invalidArgs(String[] command) {
        return command.length != 2 || invalidString(command[1]);
    }

    private void handleCommand(String commandString) {
        try {
            var command = commandString.split(" ");
            String action = command[0];
            switch (action) {
                case "\\list":
                    if (command.length == 1) {
                        var rooms = server.getAllRooms();
                        out.println(systemMessage(Arrays.toString(rooms.toArray())));
                    } else if (command.length == 2) {
                        String roomToList = command[1];
                        handlePeople(roomToList);
                    } else {
                        throw new IllegalArgumentException("Invalid argument");
                    }
                    break;
                case "\\create":
                    if (invalidArgs(command)) {
                        throw new IllegalArgumentException("Invalid argument");
                    }
                    String roomToCreate = command[1];
                    handleCreate(roomToCreate);
                    break;
                case "\\join":
                    if (invalidArgs(command)) {
                        throw new IllegalArgumentException("Invalid argument");
                    }
                    String roomToJoin = command[1];
                    handleJoin(roomToJoin);
                    break;
                case "\\shout":
                    String message = commandString.substring(action.length() + 1);
                    broadcast(shoutMessage(message));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command: " + action);
            }
        } catch (IllegalArgumentException e) {
            out.println(errorMessage(e.getMessage()));
        }
    }

    /**
     * Runs the client handler thread.
     * Handles incoming messages and commands from the client.
     * Sends messages to other clients in the same room.
     */
    @Override
    public void run() {
        try {
            out.println(systemMessage("Enter username: "));
            id = in.readLine();
            if (id.isEmpty()) {
                id = "user_" + idCounter++;
            } else {
                while (invalidString(id) && !server.setNameToUser(id)) {
                    out.print(errorMessage("Invalid username, enter a different username: "));
                    id = in.readLine();
                }
            }
            out.println(systemMessage("Welcome, " + id));
            server.addUserToRoom(this.id, "Main");
            this.room = "Main";
            broadcastRoom(systemMessage(this.id + " joined the room"));
            String message;
            while ((message = in.readLine()) != null) {
                if (message.charAt(0) == '\\') {
                    handleCommand(message);
                } else {
                    broadcastRoom(userMessage(message));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clients) {
                clients.remove(this);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}