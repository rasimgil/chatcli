import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Scanner scanner;

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

            userInputThread.join();
            receiveThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}