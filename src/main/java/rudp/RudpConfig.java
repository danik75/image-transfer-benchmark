package rudp;

import java.net.*;

public class RudpConfig {
    public static final int CHUNK_SIZE = 4096;
    public static final int ACK_TIMEOUT_MILLIS = 50;
    public static final int ACK_THRESHOLD_PERCENT = 100;
    public static final int MAX_RETRIES_PER_CHUNK = 3;
    public static final int PORT = 7002;
    public static final InetAddress HOST;

    static {
        try {
            HOST = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to resolve localhost", e);
        }
    }
}