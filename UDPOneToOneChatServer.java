import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Question 3: UDP One-to-One Chat Server
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPOneToOneChatServer {
    private static final int PORT = 9878;
    private static final int BUFFER_SIZE = 1024;
    private static volatile InetAddress clientAddress = null;
    private static volatile int clientPort = -1;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     UDP ONE-TO-ONE CHAT SERVER          ");
        System.out.println("=========================================");
        System.out.println("[INFO] Server listening on UDP Port: " + PORT);
        System.out.println("[INFO] Waiting for client to initiate chat...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT);
             Scanner scanner = new Scanner(System.in)) {

            // Thread to receive messages from client asynchronously
            Thread receiveThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        // Capture client address and port from the first incoming packet
                        if (clientAddress == null) {
                            clientAddress = packet.getAddress();
                            clientPort = packet.getPort();
                            System.out.println("\n[INFO] Client connected from " + clientAddress.getHostAddress() + 
                                               ":" + clientPort);
                            System.out.println("[INFO] Real-time chat established! Type messages below:\n");
                        }

                        String message = new String(packet.getData(), 0, packet.getLength());
                        if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("bye")) {
                            System.out.println("\n[INFO] Client ended the chat.");
                            running = false;
                            socket.close();
                            break;
                        }

                        System.out.println("\nClient > " + message);
                        System.out.print("Server > ");
                    } catch (IOException e) {
                        if (!running) break;
                    }
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main thread sends messages to client
            while (running && scanner.hasNextLine()) {
                // Wait until client has sent at least one message so we know their port
                if (clientAddress == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                    continue;
                }

                System.out.print("Server > ");
                String msgToSend = scanner.nextLine();

                if (!running) break;

                if (msgToSend != null && !msgToSend.trim().isEmpty()) {
                    byte[] sendBuffer = msgToSend.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendBuffer,
                            sendBuffer.length,
                            clientAddress,
                            clientPort
                    );
                    socket.send(sendPacket);

                    if (msgToSend.equalsIgnoreCase("exit") || msgToSend.equalsIgnoreCase("bye")) {
                        System.out.println("[INFO] Server ending chat session.");
                        running = false;
                        socket.close();
                        break;
                    }
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[ERROR] Server exception: " + e.getMessage());
            }
        }
    }
}
