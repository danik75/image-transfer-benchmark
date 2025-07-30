package udp;

public class UdpImageConfig {
    public static final int PORT = 7001;
    public static final int CHUNK_SIZE = 516; // 512 + 4 header bytes
    public static final int HEADER_SIZE = 4; // 512 + 4 header bytes

    private UdpImageConfig() {
        // Prevent instantiation
    }
}