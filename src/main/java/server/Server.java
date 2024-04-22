package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a chat room server.
 */
public class Server {
    private final Map<String, Set<String>> rooms;
    private final Set<String> names;

    /**
     * Constructs a new {@link Server} object.
     */
    public Server() {
        this.rooms = new ConcurrentHashMap<>();
        this.names = new CopyOnWriteArraySet<>();
    }

    /**
     * Starts the server on the specified port.
     * @param port The port number on which the server will listen for incoming connections.
     * @throws RuntimeException if an error occurs while starting this server.
     */
    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server.Server started on port: " + port);
            this.createRoom("Main");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client.Client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the name of a user.
     * @param name The name to be set for the user.
     * If the name is not available this method does nothing and returns false.
     * Otherwise, the name is set to the user and the method returns true.
     * @return true if the name was set for the user, false otherwise.
     */
    public boolean setNameToUser(String name) {
        return names.add(name);
    }

    /**
     * Adds a user to the specified chat room.
     * If the room does not exist, or if the user is already present in the room this method returns false.
     * If the user has been added to the room successfully this method returns true.
     * @param user The name of the user to be added.
     * @param room The name of the room which the user should be added to.
     * @return true if the user was added to the room, false otherwise.
     */
    public boolean addUserToRoom(String user, String room) {
        return rooms.containsKey(room) && rooms.get(room).add(user);
    }

    /**
     * Removes a user from the specified chat room.
     * @param user The name of the user to be removed.
     * @param room The name of the room from which the user should be removed.
     * @return true if the user was removed from the room, false otherwise.
     */
    public boolean removeUserFromRoom(String user, String room) {
        return rooms.containsKey(room)  && rooms.get(room).remove(user);
    }

    /**
     * Returns the users in a specified room.
     * @param room The name of the room to retrieve the users from.
     * @return A set with the string "false" if the room does not exist, otherwise a set containing the names of users in the specified room.
     */
    public Set<String> getUsersInRoom(String room) {
        return rooms.getOrDefault(room, Set.of("false"));
    }

    /**
     * Creates a room with the specified name if it does not already exist.
     * If the room already exists, nothing happens.
     * @param room The name of the room to be created.
     * @return true if the room has been created successfully, false if the room already exists.
     */
    public boolean createRoom(String room) {
        return rooms.putIfAbsent(room, new CopyOnWriteArraySet<>()) == null;
    }

    /**
     * Returns the set of created rooms available in the server
     * @return Set of created rooms in the server
     */
    public Set<String> getAllRooms() {
        return Set.copyOf(rooms.keySet());
    }

    /**
     * Start the server application.
     * @param args The command-line arguments. Expects one argument: The server port number.
     */
    public static void main(String[] args) {
        Server s = new Server();
        try {
            int port = Integer.parseInt(args[0]);
            s.startServer(port);
        } catch (NumberFormatException e) {
            System.out.println("Error: Port number must be a valid integer.");
        }
    }
}
