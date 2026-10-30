import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Question 2: UDP File Transfer Client
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPFileClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9877;
    private static final int CHUNK_SIZE = 1024;
    private static final int TIMEOUT_MS = 5000;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     UDP FILE TRANSFER CLIENT STARTED    ");
        System.out.println("=========================================");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            System.out.print("Enter file path to send: ");
            String filePath = scanner.nextLine().trim();

            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                System.err.println("[ERROR] File not found or is invalid: " + filePath);
                return;
            }

            String fileName = file.getName();
            long fileSize = file.length();

            System.out.println("[INFO] Sending Metadata -> " + fileName + " (" + fileSize + " bytes)");

            // 1. Send START:<filename>:<fileSize>
            String startMsg = "START:" + fileName + ":" + fileSize;
            byte[] startBytes = startMsg.getBytes();
            DatagramPacket startPacket = new DatagramPacket(startBytes, startBytes.length, serverAddress, SERVER_PORT);
            socket.send(startPacket);

            // 2. Wait for ACK_READY
            byte[] ackBuf = new byte[1024];
            DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
            try {
                socket.receive(ackPacket);
                String ack = new String(ackPacket.getData(), 0, ackPacket.getLength());
                if (!ack.equals("ACK_READY")) {
                    System.err.println("[ERROR] Server not ready: " + ack);
                    return;
                }
            } catch (SocketTimeoutException e) {
                System.err.println("[ERROR] Server failed to respond to handshake.");
                return;
            }

            System.out.println("[INFO] Server is ready. Transmitting file data chunks...");

            // 3. Transmit file chunks
            long totalSent = 0;
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] chunk = new byte[CHUNK_SIZE];
                int bytesRead;

                while ((bytesRead = fis.read(chunk)) != -1) {
                    DatagramPacket dataPacket = new DatagramPacket(chunk, bytesRead, serverAddress, SERVER_PORT);
                    socket.send(dataPacket);
                    totalSent += bytesRead;

                    // Small pause to prevent UDP buffer flooding
                    Thread.sleep(2);
                }
            } catch (InterruptedException ignored) {}

            // 4. Send END packet
            byte[] endBytes = "END".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endBytes, endBytes.length, serverAddress, SERVER_PORT);
            socket.send(endPacket);

            System.out.println("[INFO] Sent " + totalSent + " bytes. Waiting for server confirmation...");

            // 5. Receive confirmation from server
            byte[] respBuf = new byte[1024];
            DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length);
            socket.receive(respPacket);
            String confirmation = new String(respPacket.getData(), 0, respPacket.getLength());

            System.out.println("[SUCCESS] Server Response: " + confirmation);

        } catch (IOException e) {
            System.err.println("[ERROR] Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
