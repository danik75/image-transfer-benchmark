package udp;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class UdpImageServer {
    public static void main(String[] args) throws Exception {
        final int port = 7001;
        final int bufferSize = 516; // 512 + 4 header bytes
        DatagramSocket socket = new DatagramSocket(port);

        System.out.println("UDP Image Server listening on port " + port);

        while (true) {
            Map<Integer, byte[]> chunks = new TreeMap<>();
            int totalChunks = -1;

            System.out.println("Waiting for a new image...");

            while (true) {
                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                // Extract header
                int sequence = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                totalChunks = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
                byte[] chunkData = Arrays.copyOfRange(data, 4, data.length);
                chunks.put(sequence, chunkData);

                // Send ACK
                byte[] ack = new byte[]{(byte) (sequence >> 8), (byte) sequence};
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, packet.getAddress(), packet.getPort());
                socket.send(ackPacket);

                // Stop when all chunks received
                if (chunks.size() == totalChunks) {
                    System.out.println("✅ Received all " + totalChunks + " chunks. Reassembling image...");

                    try (FileOutputStream fos = new FileOutputStream("received_image.jpg")) {
                        for (byte[] part : chunks.values()) {
                            fos.write(part);
                        }
                    }

                    System.out.println("💾 Image saved as received_image.jpg");
                    break;
                }
            }
        }
    }
}