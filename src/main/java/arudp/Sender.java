package arudp;

import arudp.Chunk;
import arudp.RUDPConfig;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class Sender {
    private final DatagramSocket socket;
    private final Map<Integer, Chunk> chunkMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> retries = new ConcurrentHashMap<>();
    private final Set<Integer> acksReceived = ConcurrentHashMap.newKeySet();
    private final UUID fileId = UUID.randomUUID();

    public Sender() throws Exception {
        this.socket = new DatagramSocket(RUDPConfig.SENDER_PORT);
        listenForAcks();
    }

    public static void main(String[] args) throws Exception {
        String imagePath = "test-image.jpg";

        Sender sender = new Sender();

        sender.sendFile(imagePath);
    }


    public void sendFile(String path) throws IOException {
        long startTime = System.currentTimeMillis();
        byte[] fileData = Files.readAllBytes(new File(path).toPath());
        int totalChunks = (int) Math.ceil((double) fileData.length / RUDPConfig.CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int start = i * RUDPConfig.CHUNK_SIZE;
            int end = Math.min(start + RUDPConfig.CHUNK_SIZE, fileData.length);
            byte[] chunkData = Arrays.copyOfRange(fileData, start, end);
            Chunk chunk = new Chunk(fileId, i, totalChunks, chunkData);
            chunkMap.put(i, chunk);
            sendChunk(chunk);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("File sent in (without ack) " + ((endTime - startTime)/ 1_000_000) + " ms");
        scheduleRetries();
    }

    private void sendChunk(Chunk chunk) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1400);
        buffer.putLong(chunk.fileId.getMostSignificantBits());
        buffer.putLong(chunk.fileId.getLeastSignificantBits());
        buffer.putInt(chunk.sequence);
        buffer.putInt(chunk.totalChunks);
        buffer.put(chunk.data);

        byte[] payload = Arrays.copyOf(buffer.array(), buffer.position());
        DatagramPacket packet = new DatagramPacket(payload, payload.length,
                InetAddress.getByName(RUDPConfig.HOST), RUDPConfig.RECEIVER_PORT);
        socket.send(packet);
    }

    private void listenForAcks() {
        new Thread(() -> {
            byte[] buf = new byte[24];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    ByteBuffer bb = ByteBuffer.wrap(packet.getData());
                    long msb = bb.getLong();
                    long lsb = bb.getLong();
                    int seq = bb.getInt();
                    UUID ackId = new UUID(msb, lsb);
                    if (ackId.equals(fileId)) {
                        acksReceived.add(seq);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void scheduleRetries() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            for (Map.Entry<Integer, Chunk> entry : chunkMap.entrySet()) {
                int seq = entry.getKey();
                if (!acksReceived.contains(seq)) {
                    retries.putIfAbsent(seq, 0);
                    int retryCount = retries.get(seq);
                    if (retryCount < RUDPConfig.MAX_RETRIES) {
                        try {
                            System.out.println("Retrying chunk: " + seq);
                            sendChunk(entry.getValue());
                            retries.put(seq, retryCount + 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, RUDPConfig.ACK_TIMEOUT_MS, RUDPConfig.ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
}
