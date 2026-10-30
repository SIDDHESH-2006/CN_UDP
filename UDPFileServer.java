import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Question 2: UDP File Transfer Server
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPFileServer {
    private static final int PORT = 9877;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     UDP FILE TRANSFER SERVER STARTED    ");
        System.out.println("=========================================");
        System.out.println("[INFO] Listening on UDP Port: " + PORT);
        System.out.println("[INFO] Ready to receive files...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());

                // Protocol: START:<filename>:<fileSize>
                if (msg.startsWith("START:")) {
                    String[] parts = msg.split(":");
                    String fileName = parts[1];
                    long expectedSize = Long.parseLong(parts[2]);
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    System.out.println("[INFO] Incoming file transfer request:");
                    System.out.println("       File Name    : " + fileName);
                    System.out.println("       Expected Size: " + expectedSize + " bytes");
                    System.out.println("       Sender       : " + clientAddress.getHostAddress() + ":" + clientPort);

                    // Send ACK to start receiving data
                    byte[] ack = "ACK_READY".getBytes();
                    socket.send(new DatagramPacket(ack, ack.length, clientAddress, clientPort));

                    File outputFile = new File("received_" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        long totalReceived = 0;
                        boolean receiving = true;

                        while (receiving) {
                            byte[] chunkBuffer = new byte[BUFFER_SIZE];
                            DatagramPacket chunkPacket = new DatagramPacket(chunkBuffer, chunkBuffer.length);
                            socket.receive(chunkPacket);

                            // Check if this is the END signal
                            if (chunkPacket.getLength() >= 3) {
                                String endCheck = new String(chunkPacket.getData(), 0, chunkPacket.getLength());
                                if (endCheck.equals("END")) {
                                    receiving = false;
                                    break;
                                }
                            }

                            fos.write(chunkPacket.getData(), 0, chunkPacket.getLength());
                            totalReceived += chunkPacket.getLength();
                        }

                        System.out.println("[SUCCESS] File received completely!");
                        System.out.println("          Saved As      : " + outputFile.getName());
                        System.out.println("          Total Bytes   : " + totalReceived + " bytes\n");

                        // Send completion ACK to client
                        String response = "SUCCESS: " + totalReceived + " bytes received.";
                        byte[] respBytes = response.getBytes();
                        socket.send(new DatagramPacket(respBytes, respBytes.length, clientAddress, clientPort));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
