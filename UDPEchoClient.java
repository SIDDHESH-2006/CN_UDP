import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Question 1: UDP Echo Client
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPEchoClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;
    private static final int TIMEOUT_MS = 3000;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("        UDP ECHO CLIENT STARTED          ");
        System.out.println("=========================================");
        System.out.println("[INFO] Connecting to " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("[INFO] Type any message to send. Type 'exit' to quit.\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Client > ");
                String message = scanner.nextLine();

                if (message == null || message.trim().isEmpty()) {
                    continue;
                }

                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        serverAddress,
                        SERVER_PORT
                );
                socket.send(sendPacket);

                // Receive Echo from Server
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                try {
                    socket.receive(receivePacket);
                    String echoMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Echoed from Server > " + echoMessage + "\n");
                } catch (SocketTimeoutException e) {
                    System.err.println("[TIMEOUT] Server did not respond within " + TIMEOUT_MS + " ms.\n");
                }

                if (message.trim().equalsIgnoreCase("exit")) {
                    System.out.println("[INFO] Terminating client session.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
