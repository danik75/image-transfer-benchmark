package grcp;

public class GrcpImageConfig {
    public static final int PORT = 6000; // gRPC port for image transfer
    public static final String SERVICE_NAME = "ImageService"; // gRPC service name
    public static final String METHOD_NAME = "SendImage"; // gRPC method name for sending images
    public static final String TARGET = "localhost"; // gRPC method name for sending images

    private GrcpImageConfig() {
        // Prevent instantiation
    }
}
