package rudp;

import java.nio.ByteBuffer;
import java.util.UUID;

public class RudpHeader {
    public static final int HEADER_SIZE = 64 + 16 + 2 + 4 + 2 + 2;

    public static byte[] encode(String filename, UUID fileId, int chunkSize, int totalSize, int chunkIndex, int totalChunks) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        byte[] nameBytes = filename.getBytes();
        byte[] paddedName = new byte[64];
        System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 64));
        buffer.put(paddedName);
        buffer.putLong(fileId.getMostSignificantBits());
        buffer.putLong(fileId.getLeastSignificantBits());
        buffer.putShort((short) chunkSize);
        buffer.putInt(totalSize);
        buffer.putShort((short) chunkIndex);
        buffer.putShort((short) totalChunks);
        return buffer.array();
    }

    public static String getFilename(byte[] header) {
        return new String(header, 0, 64).trim();
    }

    public static UUID getFileId(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header, 64, 16);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static int getChunkSize(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header, 80, 2);
        return buffer.getShort() & 0xFFFF;
    }

    public static int getTotalFileSize(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header, 82, 4);
        return buffer.getInt();
    }

    public static int getChunkIndex(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header, 86, 2);
        return buffer.getShort() & 0xFFFF;
    }

    public static int getTotalChunks(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header, 88, 2);
        return buffer.getShort() & 0xFFFF;
    }
}