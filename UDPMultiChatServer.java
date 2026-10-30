import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Question 4: UDP Multi-Client Chat Server with Broadcasting
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPMultiChatServer {
    private static final int PORT = 9879;
    private static final int BUFFER_SIZE = 2048;

    // Map storing client SocketAddress -> Username
    private static final Map<SocketAddress, String> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  UDP MULTI-CLIENT BROADCAST CHAT SERVER STARTED  ");
        System.out.println("==================================================");
        System.out.println("[INFO] Server listening on UDP Port: " + PORT);
        System.out.println("[INFO] Ready to route messages between clients...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                SocketAddress senderAddress = packet.getSocketAddress();
                String rawMessage = new String(packet.getData(), 0, packet.getLength()).trim();

                if (rawMessage.startsWith("JOIN:")) {
                    String username = rawMessage.substring(5).trim();
                    clients.put(senderAddress, username);
                    System.out.println("[JOIN] " + username + " connected from " + senderAddress);
                    System.out.println("[INFO] Active clients count: " + clients.size());

                    String notification = ">>> [SERVER] " + username + " joined the chat room. (" + clients.size() + " online)";
                    broadcast(socket, notification, senderAddress);

                    // Send welcome acknowledgment to the new client
                    sendDirect(socket, ">>> Welcome to the chat room, " + username + "! Type 'exit' to leave.", senderAddress);

                } else if (rawMessage.startsWith("LEAVE:")) {
                    String username = clients.remove(senderAddress);
                    if (username == null) {
                        username = rawMessage.substring(6).trim();
                    }
                    System.out.println("[LEAVE] " + username + " disconnected.");
                    System.out.println("[INFO] Active clients count: " + clients.size());

                    String notification = ">>> [SERVER] " + username + " has left the chat room.";
                    broadcast(socket, notification, senderAddress);

                } else if (rawMessage.startsWith("MSG:")) {
                    // Protocol: MSG:username:content
                    int firstColon = rawMessage.indexOf(':', 4);
                    if (firstColon != -1) {
                        String username = rawMessage.substring(4, firstColon);
                        String content = rawMessage.substring(firstColon + 1);

                        System.out.println("[MSG] [" + username + "] -> " + content);

                        String broadcastMsg = "[" + username + "]: " + content;
                        broadcast(socket, broadcastMsg, senderAddress);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients except optionally the sender
     */
    private static void broadcast(DatagramSocket socket, String message, SocketAddress sender) {
        byte[] data = message.getBytes();
        for (SocketAddress clientAddress : clients.keySet()) {
            if (!clientAddress.equals(sender)) {
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress);
                    socket.send(packet);
                } catch (IOException e) {
                    System.err.println("[WARN] Failed sending to " + clientAddress + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Sends a direct packet to a specific client address
     */
    private static void sendDirect(DatagramSocket socket, String message, SocketAddress recipient) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, recipient);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("[WARN] Direct send failed: " + e.getMessage());
        }
    }
}
