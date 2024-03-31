import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        scanner = new Scanner(System.in);
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

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

    public static void main(String[] args) {
        try {
            Client client = new Client("localhost", 1234);
            Thread userInputThread = new Thread(client::sendUserInput);
            Thread receiveThread = new Thread(client::receiveMessages);

            userInputThread.start();
            receiveThread.start();

            userInputThread.join(); // Wait for the user input thread to finish
            receiveThread.join(); // Wait for the receive thread to finish
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}