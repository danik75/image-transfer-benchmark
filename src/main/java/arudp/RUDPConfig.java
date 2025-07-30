package arudp;

public class RUDPConfig {
    public static final int CHUNK_SIZE = 1024; // bytes
    public static final int ACK_TIMEOUT_MS = 500;
    public static final int MAX_RETRIES = 5;
    public static final int SENDER_PORT = 7001;
    public static final int RECEIVER_PORT = 7002;
    public static final String HOST = "127.0.0.1";
}
