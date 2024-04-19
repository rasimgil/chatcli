import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private final BufferedReader in;
    private final PrintWriter out;
    public String id;
    public String room;
    private static final Set<ClientHandler> clients = new HashSet<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW_BOLD = "\033[1;33m";
    public static final String ANSI_RED_RED = "\033[1;31m";

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
                if (client != this && Objects.equals(client.room, this.room)) {
                    client.out.println(message);
                }
            }
        }
    }

    private String systemMessage(String message) {
        return ANSI_YELLOW_BOLD + message + ANSI_RESET;
    }

    private String errorMessage(String message) {
        return ANSI_RED_RED + message + ANSI_RESET;
    }

    private void handleCommand(String message) {
        var command = message.substring(1).split(" ");
        String action = command[0];
        switch (action) {
            case "list":
                var rooms = server.getAllRooms();
                String listMessage = String.format("[Server]: %s", Arrays.toString(rooms.toArray()));
                out.println(systemMessage(listMessage));
                break;
            case "create":
                String roomToCreate = command[1];
                if (!server.createRoom(roomToCreate)) {
                    String errorCreateMessage = String.format("[Server]: Error creating %s", roomToCreate);
                    out.println(errorMessage(errorCreateMessage));
                } else {
                    String createMessage = String.format("[Server]: Created %s", roomToCreate);
                    out.println(systemMessage(createMessage));
                }
                break;
            case "join":
                String roomToJoin = command[1];
                if (!server.addUserToRoom(id, roomToJoin)) {
                    String errorJoinRoom = String.format("[Server]: Error joining %s", roomToJoin);
                    out.println(errorMessage(errorJoinRoom));
                }
                else {
                    String currentRoom = this.room;
                    String leaveNotifyMessage = String.format("[Server]: %s left the room", this.id);
                    broadcast(systemMessage(leaveNotifyMessage));
                    server.removeUserFromRoom(id, currentRoom);
                    this.room = roomToJoin;
                    String joinMessage = String.format("[Server]: Joined %s", roomToJoin);
                    out.println(systemMessage(joinMessage));
                    String joinNotifyMessage = String.format("[Server]: %s joined the room", this.id);
                    broadcast(systemMessage(joinNotifyMessage));
                }
                break;
            case "people":
                String roomToList = command[1];
                var users = server.getUsersInRoom(roomToList);
                String peopleMessage = String.format("[Server]: %s%s", roomToList, Arrays.toString(users.toArray()));
                out.println(systemMessage(peopleMessage));
                break;
            default:
                out.println("unknown command");
        }
    }

    @Override
    public void run() {
        try {
            String userNameMessage = "[Server]: Enter username: ";
            out.println(systemMessage(userNameMessage));
            id = in.readLine();
            while (!server.setNameToUser(id)) {
                String badNameError = "Username not available, enter a different username: ";
                out.print(errorMessage(badNameError));
                id = in.readLine();
            }
            String greetMessage = "Welcome, " + id;
            out.println(systemMessage(greetMessage));
            server.addUserToRoom(this.id, "Main");
            this.room = "Main";

            String message;
            while ((message = in.readLine()) != null) {
                if (message.charAt(0) == '\\') {
                    handleCommand(message);
                } else {
                    String s = String.format("%s[%s]: %s", this.room, this.id, message);
                    broadcast(ANSI_CYAN + s + ANSI_RESET);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}