package rudp;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class RudpSender {
    public long send(String filePath) throws IOException {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        UUID fileId = UUID.randomUUID();
        String fileName = Paths.get(filePath).getFileName().toString();

        long startTimer = System.nanoTime();

        int totalChunks = (int) Math.ceil((double) fileData.length / (RudpConfig.CHUNK_SIZE - RudpHeader.HEADER_SIZE));
        Map<Integer, byte[]> chunks = new HashMap<>();

        for (int i = 0; i < totalChunks; i++) {
            int start = i * (RudpConfig.CHUNK_SIZE - RudpHeader.HEADER_SIZE);
            int end = Math.min(start + (RudpConfig.CHUNK_SIZE - RudpHeader.HEADER_SIZE), fileData.length);
            byte[] chunkData = Arrays.copyOfRange(fileData, start, end);
            byte[] header = RudpHeader.encode(fileName, fileId, RudpConfig.CHUNK_SIZE, fileData.length, i, totalChunks);
            byte[] packet = new byte[header.length + chunkData.length];
            System.arraycopy(header, 0, packet, 0, header.length);
            System.arraycopy(chunkData, 0, packet, header.length, chunkData.length);
            chunks.put(i, packet);
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(RudpConfig.ACK_TIMEOUT_MILLIS);
            Set<Integer> acknowledged = new HashSet<>();

            for (int attempt = 0; attempt < RudpConfig.MAX_RETRIES_PER_CHUNK; attempt++) {
                for (int i = 0; i < totalChunks; i++) {
                    if (!acknowledged.contains(i)) {
                        byte[] packet = chunks.get(i);
                        DatagramPacket datagram = new DatagramPacket(packet, packet.length, RudpConfig.HOST, RudpConfig.PORT);
                        socket.send(datagram);
                    }
                }

                long endWait = System.currentTimeMillis() + RudpConfig.ACK_TIMEOUT_MILLIS;
                while (System.currentTimeMillis() < endWait) {
                    try {
                        byte[] ackBuf = new byte[2];
                        DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
                        socket.receive(ackPacket);
                        int ackIndex = ((ackBuf[0] & 0xFF) << 8) | (ackBuf[1] & 0xFF);
                        acknowledged.add(ackIndex);
                    } catch (SocketTimeoutException ignore) {}
                }

                int ackCount = acknowledged.size();
                double ackPercent = (ackCount * 100.0) / totalChunks;
                if (ackPercent >= RudpConfig.ACK_THRESHOLD_PERCENT) {
                    System.out.println("Transfer complete: " + ackCount + "/" + totalChunks + " chunks acknowledged.");
                    long end = System.nanoTime();
                    return (end - startTimer) / 1_000_000;
                }
            }
            System.err.println("Transfer failed: not enough chunks acknowledged.");
        }
        return -1;
    }
}