package common;

import io.grpc.MethodDescriptor;

import java.io.*;

public class ByteArrayMarshaller implements MethodDescriptor.Marshaller<byte[]> {
    @Override
    public InputStream stream(byte[] value) {
        return new ByteArrayInputStream(value);
    }

    @Override
    public byte[] parse(InputStream stream) {
        try {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}