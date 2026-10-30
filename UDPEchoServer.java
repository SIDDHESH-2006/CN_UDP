import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Question 1: UDP Echo Server
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPEchoServer {
    private static final int PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("       UDP ECHO SERVER STARTED           ");
        System.out.println("=========================================");
        System.out.println("[INFO] Listening on UDP Port: " + PORT);
        System.out.println("[INFO] Waiting for client messages...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("[RECEIVED] From " + receivePacket.getAddress().getHostAddress() + 
                                   ":" + receivePacket.getPort() + " -> \"" + clientMessage + "\"");

                // Echo back the exact same message to client
                byte[] sendBuffer = clientMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                );
                socket.send(sendPacket);
                System.out.println("[ECHOED] Sent back to " + receivePacket.getAddress().getHostAddress() + 
                                   ":" + receivePacket.getPort() + "\n");

                if (clientMessage.trim().equalsIgnoreCase("exit")) {
                    System.out.println("[INFO] Exit signal received. Server shutting down.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
