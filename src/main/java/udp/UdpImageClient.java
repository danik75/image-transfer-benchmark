// UdpImageClient.java
package udp;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UdpImageClient {
    public static long send(String imagePath) throws IOException {
        final int port = UdpImageConfig.PORT; // Use the port defined in UdpImageConfig
        final int chunkSize = UdpImageConfig.CHUNK_SIZE;
        final int headerSize = UdpImageConfig.HEADER_SIZE;

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
        }
        long end = System.nanoTime();
        socket.close();
        return (end - start) / 1_000_000;
    }
}