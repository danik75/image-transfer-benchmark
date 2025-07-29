package tcp;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class TcpImageClient {
    public static long send(String imagePath) throws IOException {
        byte[] imageData = Files.readAllBytes(Paths.get(imagePath));
        long start = System.nanoTime();

        try (
                Socket socket = new Socket("localhost", 5001);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream()
        ) {
            // Send image data
            out.write(imageData);
            out.flush();
            socket.shutdownOutput(); // 🚨 Signal end of data to server

            System.out.println("Image sent. Waiting for acknowledgment...");

            // Read server acknowledgment
            int ack = in.read();
            if (ack == -1) {
                System.err.println("No acknowledgment received.");
            } else {
                System.out.println("Received ACK: " + ack);
            }

        } catch (IOException e) {
            System.err.println("TCP client error: " + e.getMessage());
            throw e;
        }

        long end = System.nanoTime();
        return (end - start) / 1_000_000; // return elapsed time in ms
    }
}