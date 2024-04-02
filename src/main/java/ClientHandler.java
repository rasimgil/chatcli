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

    private void handleCommand(String message) {
        var command = message.substring(1).split(" ");
        String action = command[0];
        switch (action) {
            case "list":
                var rooms = server.getAllRooms();
                out.println("Listing rooms: ");
                out.println(Arrays.toString(rooms.toArray()));
                break;
            case "create":
                String roomToCreate = command[1];
                out.println("Creating room: " + roomToCreate);
                if (!server.createRoom(roomToCreate)) {
                    out.println("Error creating room: "  + roomToCreate);
                }
                break;
            case "join":
                String roomToJoin = command[1];
                out.println("Joining room: " + roomToJoin);
                if (!server.addUserToRoom(id, roomToJoin)) {
                    out.println("Error joining room: " + roomToJoin);
                }
                else {
                    this.room = roomToJoin;
                }
                break;
            case "people":
                String roomToList = command[1];
                var users = server.getUsersInRoom(roomToList);
                out.println("Participants in room " + roomToList + ":");
                out.println(Arrays.toString(users.toArray()));
                break;
            default:
                out.println("unknown command");
        }
    }

    @Override
    public void run() {
        try {
            out.println("Enter username: ");
            id = in.readLine();
            while (!server.setNameToUser(id)) {
                out.print("Username not available, enter a different username: ");
                id = in.readLine();
            }
            out.println("Welcome, " + id);
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