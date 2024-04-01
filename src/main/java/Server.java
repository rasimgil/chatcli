import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server {
    private final Map<String, Set<String>> rooms;
    private final Set<String> names;

    public Server() {
        this.rooms = new ConcurrentHashMap<>();
        this.names = new CopyOnWriteArraySet<>();
    }

    public void startServer() {
        int port = 1234;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: ");
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setNameToUser(String name) {
        return names.add(name);
    }

    public boolean addUserToRoom(String user, String room) {
        return rooms.containsKey(room) && rooms.get(room).add(user);
    }

    public boolean removeUserFromRoom(String user, String room) {
        return rooms.containsKey(room) && rooms.get(room).remove(user);
    }

    public Set<String> getUsersInRoom(String room) {
        return rooms.getOrDefault(room, Set.of());
    }

    public boolean createRoom(String room) {
        return rooms.putIfAbsent(room, new CopyOnWriteArraySet<>()) == null;
    }

    public List<String> getAllRooms() {
        return List.copyOf(rooms.keySet());
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
