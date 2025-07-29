package grcp;

import common.ByteArrayMarshaller;
import io.grpc.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GrpcImageClient {
    public static long send(String imagePath) throws IOException {
        MethodDescriptor<byte[], byte[]> method = MethodDescriptor.<byte[], byte[]>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName("ImageService", "SendImage"))
                .setRequestMarshaller(new ByteArrayMarshaller())
                .setResponseMarshaller(new ByteArrayMarshaller())
                .build();

        ManagedChannel channel = Grpc.newChannelBuilder("localhost:6000", InsecureChannelCredentials.create()).build();
        ClientCall<byte[], byte[]> call = channel.newCall(method, CallOptions.DEFAULT);

        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        long start = System.nanoTime();

        final Object lock = new Object();
        final byte[][] responseHolder = new byte[1][];
        final Throwable[] errorHolder = new Throwable[1];

        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(byte[] message) {
                responseHolder[0] = message;
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    errorHolder[0] = status.asRuntimeException(trailers);
                }
                synchronized (lock) {
                    lock.notify();
                }
            }
        }, new Metadata());

        call.sendMessage(imageBytes);
        call.halfClose();
        call.request(1); // Request one response

        synchronized (lock) {
            try {
                lock.wait(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        channel.shutdownNow();

        if (errorHolder[0] != null) {
            throw new RuntimeException("gRPC failed", errorHolder[0]);
        }

        long duration = System.nanoTime() - start;
//        System.out.println("gRPC response: " + new String(responseHolder[0]));
        return duration / 1_000_000;
    }
}