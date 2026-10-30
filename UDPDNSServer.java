import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Question 5: UDP DNS Server
 * Course: BITE305P - Computer Networks Lab
 * Student: Siddhesh Srinivasan
 */
public class UDPDNSServer {
    private static final int PORT = 9880;
    private static final int BUFFER_SIZE = 1024;

    // Static DNS lookup table / cache
    private static final Map<String, String> dnsTable = new HashMap<>();

    static {
        dnsTable.put("google.com", "142.250.190.46");
        dnsTable.put("www.google.com", "142.250.190.46");
        dnsTable.put("vit.ac.in", "103.227.100.10");
        dnsTable.put("www.vit.ac.in", "103.227.100.10");
        dnsTable.put("github.com", "140.82.121.4");
        dnsTable.put("www.github.com", "140.82.121.4");
        dnsTable.put("wikipedia.org", "208.80.154.224");
        dnsTable.put("www.wikipedia.org", "208.80.154.224");
        dnsTable.put("youtube.com", "172.217.168.206");
        dnsTable.put("www.youtube.com", "172.217.168.206");
        dnsTable.put("localhost", "127.0.0.1");
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("       UDP DNS SERVER STARTED            ");
        System.out.println("=========================================");
        System.out.println("[INFO] Server listening on UDP Port: " + PORT);
        System.out.println("[INFO] Preloaded " + dnsTable.size() + " DNS records in cache.");
        System.out.println("[INFO] Ready for DNS resolution queries...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String query = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                System.out.println("[QUERY] Client " + receivePacket.getAddress().getHostAddress() + 
                                   ":" + receivePacket.getPort() + " requested DNS lookup for: \"" + query + "\"");

                // Clean domain from URL if protocol/paths are passed (e.g. https://www.google.com/)
                String domain = cleanDomain(query);

                // 1. Check local DNS table
                String resolvedIp = dnsTable.get(domain.toLowerCase());

                // 2. Dynamic lookup fallback
                if (resolvedIp == null) {
                    try {
                        InetAddress addr = InetAddress.getByName(domain);
                        resolvedIp = addr.getHostAddress();
                        dnsTable.put(domain.toLowerCase(), resolvedIp); // cache it
                        System.out.println("[RESOLVE] Resolved dynamically via DNS: " + resolvedIp);
                    } catch (UnknownHostException e) {
                        resolvedIp = "ERROR: Domain name could not be resolved.";
                        System.out.println("[RESOLVE] FAILED: Domain not found");
                    }
                } else {
                    System.out.println("[RESOLVE] Resolved from local DNS cache: " + resolvedIp);
                }

                // Send response back to client
                byte[] sendBuffer = resolvedIp.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                );
                socket.send(sendPacket);
                System.out.println("[SENT] IP address returned to client.\n");
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String cleanDomain(String url) {
        String clean = url.trim();
        if (clean.startsWith("http://")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("https://")) {
            clean = clean.substring(8);
        }
        int slashIndex = clean.indexOf('/');
        if (slashIndex != -1) {
            clean = clean.substring(0, slashIndex);
        }
        return clean;
    }
}
