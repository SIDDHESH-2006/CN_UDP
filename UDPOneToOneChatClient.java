import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Question 3: UDP One-to-One Chat Client
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPOneToOneChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9878;
    private static final int BUFFER_SIZE = 1024;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     UDP ONE-TO-ONE CHAT CLIENT          ");
        System.out.println("=========================================");
        System.out.println("[INFO] Connecting to Chat Server at " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("[INFO] Type 'exit' or 'bye' to end conversation.\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // Asynchronous thread to receive messages from server
            Thread receiveThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String message = new String(packet.getData(), 0, packet.getLength());
                        if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("bye")) {
                            System.out.println("\n[INFO] Server ended the chat.");
                            running = false;
                            socket.close();
                            break;
                        }

                        System.out.println("\nServer > " + message);
                        System.out.print("Client > ");
                    } catch (IOException e) {
                        if (!running) break;
                    }
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main thread sends messages to server
            while (running && scanner.hasNextLine()) {
                System.out.print("Client > ");
                String msgToSend = scanner.nextLine();

                if (!running) break;

                if (msgToSend != null && !msgToSend.trim().isEmpty()) {
                    byte[] sendBuffer = msgToSend.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendBuffer,
                            sendBuffer.length,
                            serverAddress,
                            SERVER_PORT
                    );
                    socket.send(sendPacket);

                    if (msgToSend.equalsIgnoreCase("exit") || msgToSend.equalsIgnoreCase("bye")) {
                        System.out.println("[INFO] Client ending chat session.");
                        running = false;
                        socket.close();
                        break;
                    }
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[ERROR] Client exception: " + e.getMessage());
            }
        }
    }
}
