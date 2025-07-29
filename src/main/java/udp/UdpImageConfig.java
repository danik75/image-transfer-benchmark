package udp;

public class UdpImageConfig {
    public static final int PORT = 7001;
    public static final int CHUNK_SIZE = 2000; // 512 + 4 header bytes
    public static final int HEADER_SIZE = 4; // 512 + 4 header bytes
    public static final String OUTPUT_FILE_NAME = "received_image.jpg";

    private UdpImageConfig() {
        // Prevent instantiation
    }
}