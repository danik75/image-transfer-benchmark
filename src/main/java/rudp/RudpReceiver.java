package rudp;

import java.io.*;
import java.net.*;
import java.util.*;

public class RudpReceiver {

    public static void main(String[] args) throws IOException {
        new RudpReceiver().start();
    }
    private final Map<UUID, Map<Integer, byte[]>> receivedChunks = new HashMap<>();
    private final Map<UUID, Integer> totalChunksMap = new HashMap<>();
    private final Map<UUID, String> fileNames = new HashMap<>();

    public RudpReceiver() {

    }

    public void start() throws IOException {
        try (DatagramSocket socket = new DatagramSocket(RudpConfig.PORT)) {
            byte[] buf = new byte[1500];
            System.out.println("RUDP Receiver listening on port " + RudpConfig.PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
                byte[] header = Arrays.copyOfRange(data, 0, RudpHeader.HEADER_SIZE);
                byte[] payload = Arrays.copyOfRange(data, RudpHeader.HEADER_SIZE, data.length);

                UUID fileId = RudpHeader.getFileId(header);
                int chunkIndex = RudpHeader.getChunkIndex(header);
                int totalChunks = RudpHeader.getTotalChunks(header);
                String filename = RudpHeader.getFilename(header);

                receivedChunks.putIfAbsent(fileId, new HashMap<>());
                receivedChunks.get(fileId).put(chunkIndex, payload);
                totalChunksMap.put(fileId, totalChunks);
                fileNames.putIfAbsent(fileId, filename);

                // Send ACK
                byte[] ack = new byte[2];
                ack[0] = (byte) (chunkIndex >> 8);
                ack[1] = (byte) (chunkIndex);
                socket.send(new DatagramPacket(ack, ack.length, packet.getSocketAddress()));

                // Check completion
                if (receivedChunks.get(fileId).size() == totalChunks) {
                    System.out.println("All chunks received for file: " + filename);
                    saveFile(fileId);
                    receivedChunks.remove(fileId);
                    totalChunksMap.remove(fileId);
                    fileNames.remove(fileId);
                }
            }
        }
    }

    private void saveFile(UUID fileId) throws IOException {
        Map<Integer, byte[]> chunks = receivedChunks.get(fileId);
        String fileName = fileNames.get(fileId);
        try (FileOutputStream fos = new FileOutputStream("received_" + fileName)) {
            for (int i = 0; i < totalChunksMap.get(fileId); i++) {
                fos.write(chunks.get(i));
            }
        }
        System.out.println("File saved: received_" + fileName);
    }
}
