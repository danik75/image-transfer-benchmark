// UdpReliableImageClient.java
package rrdp;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UdpReliableImageClient {
    public static long send(String imagePath) throws IOException {
        final int port = 7002; // Different port for URDP
        final int chunkSize = 20000;
        final int headerSize = 4; // 2 bytes sequence, 2 bytes total

        InetAddress address = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket();
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        int totalChunks = (int) Math.ceil(imageBytes.length / (double) chunkSize);

        long start = System.nanoTime();
        for (int i = 0; i < totalChunks; i++) {
            int startIdx = i * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, imageBytes.length);
            byte[] chunk = new byte[endIdx - startIdx];
            System.arraycopy(imageBytes, startIdx, chunk, 0, chunk.length);

            byte[] packet = new byte[chunk.length + headerSize];
            packet[0] = (byte) (i >> 8);
            packet[1] = (byte) i;
            packet[2] = (byte) (totalChunks >> 8);
            packet[3] = (byte) totalChunks;
            System.arraycopy(chunk, 0, packet, 4, chunk.length);

            DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, address, port);
            socket.send(datagramPacket);

            // Wait for ACK
            byte[] ackBuffer = new byte[2];
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
            socket.receive(ackPacket);

            // Verify ACK
            int ackSequence = ((ackBuffer[0] & 0xFF) << 8) | (ackBuffer[1] & 0xFF);
            if (ackSequence != i) {
                i--; // Resend the current chunk if ACK is incorrect
            }
        }
        long end = System.nanoTime();
        socket.close();
        return (end - start) / 1_000_000;
    }
}