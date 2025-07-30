package arudp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver {
    private final DatagramSocket socket;
    private final Map<Integer, byte[]> receivedChunks = new ConcurrentHashMap<>();
    private int totalChunks;

    public Receiver() throws Exception {
        socket = new DatagramSocket(RUDPConfig.RECEIVER_PORT);
    }

    public static void main(String[] args) throws Exception {
        new Receiver().receiveLoop();
    }

    public void receiveLoop() throws Exception {
        byte[] buf = new byte[1050];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            ByteBuffer bb = ByteBuffer.wrap(packet.getData());
            long msb = bb.getLong();
            long lsb = bb.getLong();
            UUID fileId = new UUID(msb, lsb);
            int seq = bb.getInt();
            int total = bb.getInt();

            byte[] chunkData = new byte[packet.getLength() - 24];
            bb.get(chunkData);
            receivedChunks.put(seq, chunkData);
            totalChunks = total;

            sendAck(fileId, seq, packet.getAddress(), packet.getPort());

            if (receivedChunks.size() == totalChunks) {
                System.out.println("All chunks received.");
                break;
            }
        }
        rebuildFile();
    }

    private void sendAck(UUID fileId, int seq, InetAddress addr, int port) throws Exception {
        ByteBuffer ackBuf = ByteBuffer.allocate(20);
        ackBuf.putLong(fileId.getMostSignificantBits());
        ackBuf.putLong(fileId.getLeastSignificantBits());
        ackBuf.putInt(seq);
        byte[] ackBytes = ackBuf.array();
        DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, addr, RUDPConfig.SENDER_PORT);
        socket.send(ackPacket);
    }

    private void rebuildFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < totalChunks; i++) {
            baos.write(receivedChunks.get(i));
        }
        Files.write(new File("arudp_received_output.jpg").toPath(), baos.toByteArray());
        System.out.println("Image rebuilt and saved.");
    }
}
