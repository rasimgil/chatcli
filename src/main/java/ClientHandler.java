import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private BufferedReader in;
    private PrintWriter out;
    private String id;
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
                if (client != this) {
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
                broadcast(id + ": " + message);
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