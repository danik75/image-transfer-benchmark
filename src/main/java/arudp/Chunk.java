package arudp;

import java.util.UUID;

public class Chunk {
    public UUID fileId;
    public int sequence;
    public int totalChunks;
    public byte[] data;

    public Chunk(UUID fileId, int sequence, int totalChunks, byte[] data) {
        this.fileId = fileId;
        this.sequence = sequence;
        this.totalChunks = totalChunks;
        this.data = data;
    }
}