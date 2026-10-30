import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Question 4: UDP Multi-Client Chat Client
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPMultiChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9879;
    private static final int BUFFER_SIZE = 2048;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         UDP MULTI-CLIENT CHAT CLIENT             ");
        System.out.println("==================================================");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                username = "User_" + (int)(Math.random() * 1000);
            }

            // Send JOIN packet to server
            String joinMsg = "JOIN:" + username;
            byte[] joinBytes = joinMsg.getBytes();
            socket.send(new DatagramPacket(joinBytes, joinBytes.length, serverAddress, SERVER_PORT));

            System.out.println("[INFO] Joined chat as '" + username + "'. Type messages below (or 'exit' to quit):\n");

            // Background receiver thread for broadcast messages
            Thread receiverThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(received);
                    } catch (IOException e) {
                        if (!running) break;
                    }
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Main loop to send messages
            while (running && scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if (!running) break;

                if (input == null || input.trim().isEmpty()) {
                    continue;
                }

                if (input.trim().equalsIgnoreCase("exit")) {
                    // Send LEAVE notification to server
                    String leaveMsg = "LEAVE:" + username;
                    byte[] leaveBytes = leaveMsg.getBytes();
                    socket.send(new DatagramPacket(leaveBytes, leaveBytes.length, serverAddress, SERVER_PORT));
                    System.out.println("[INFO] You left the chat room.");
                    running = false;
                    socket.close();
                    break;
                }

                // Send regular message
                String msgPayload = "MSG:" + username + ":" + input;
                byte[] msgBytes = msgPayload.getBytes();
                socket.send(new DatagramPacket(msgBytes, msgBytes.length, serverAddress, SERVER_PORT));
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[ERROR] Client exception: " + e.getMessage());
            }
        }
    }
}
