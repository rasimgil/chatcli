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

    @Override
    public void run() {
        try {
            out.println("Enter username: ");
            id = in.readLine();
            while (!server.setNameToUser(id)) {
                out.println("Username not available, enter a different username: ");
                id = in.readLine();
            }
            out.println("Welcome, " + id);
            String message;
            while ((message = in.readLine()) != null) {
                if (message.charAt(0) == '\\') {
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
                            server.createRoom(roomToCreate);
                            break;
                        case "join":
                            String roomToJoin = command[1];
                            out.println("Joining room: " + roomToJoin);
                            server.addUserToRoom(id, roomToJoin);
                            this.room = roomToJoin;
                            break;
                        case "people":
                            String roomToList = command[1];
                            var users = server.getUsersInRoom(roomToList);
                            out.println("Participants in room " + roomToList + ":");
                            out.println(Arrays.toString(users.toArray()));
                            break;
                        default:
                            broadcast("unknown command");
                    }
                } else {
                    broadcast(id + ": " + message);
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