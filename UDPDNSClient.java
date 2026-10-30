import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Question 5: UDP DNS Client
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPDNSClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9880;
    private static final int BUFFER_SIZE = 1024;
    private static final int TIMEOUT_MS = 3000;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("         UDP DNS CLIENT STARTED          ");
        System.out.println("=========================================");
        System.out.println("[INFO] DNS Server at " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("[INFO] Enter domain/URL to resolve or 'exit' to quit.\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Enter URL / Domain > ");
                String domain = scanner.nextLine().trim();

                if (domain.isEmpty()) {
                    continue;
                }

                if (domain.equalsIgnoreCase("exit")) {
                    System.out.println("[INFO] Terminating DNS Client.");
                    break;
                }

                byte[] sendBuffer = domain.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        serverAddress,
                        SERVER_PORT
                );
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                try {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("DNS Server Response > " + response + "\n");
                } catch (SocketTimeoutException e) {
                    System.err.println("[TIMEOUT] DNS Server did not respond within " + TIMEOUT_MS + " ms.\n");
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
