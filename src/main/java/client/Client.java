package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Represents a client for connecting to a chat room server
 */
public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Scanner scanner;

    /**
     * Constructs a new {@link Client} object and connects it to the specified server.
     * @param ip The IP address of the server.
     * @param port The port number of the server.
     * @throws IOException if an I/O error occurs during construction.
     */
    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        scanner = new Scanner(System.in);
    }

    /**
     * Sends a message to the server.
     * @param msg The message to be sent.
     */
    public void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Receives messages from the server and prints them to the console.
     */
    public void receiveMessages() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the user input from the console and sends it to the server.
     */
    public void sendUserInput() {
        try {
            String userInput;
            while ((userInput = scanner.nextLine()) != null) {
                sendMessage(userInput);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start the client application.
     * @param args The command-line arguments. Expects two arguments: The server IP address and the server port number.
     */
    public static void main(String[] args) {
        try {
            String address = args[0];
            int port = Integer.parseInt(args[1]);
            Client client = new Client(address, port);
            Thread userInputThread = new Thread(client::sendUserInput);
            Thread receiveThread = new Thread(client::receiveMessages);

            userInputThread.start();
            receiveThread.start();

            userInputThread.join();
            receiveThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: Cannot connect to the server.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Port number must be a valid integer.");
        }
    }
}