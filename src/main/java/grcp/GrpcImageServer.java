package grcp;

import common.ByteArrayMarshaller;
import io.grpc.*;

public class GrpcImageServer {
    public static void main(String[] args) throws Exception {
        MethodDescriptor<byte[], byte[]> method = MethodDescriptor.<byte[], byte[]>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName("ImageService", "SendImage"))
                .setRequestMarshaller(new ByteArrayMarshaller())
                .setResponseMarshaller(new ByteArrayMarshaller())
                .build();

        ServerServiceDefinition service = ServerServiceDefinition.builder("ImageService")
                .addMethod(method, new ServerCallHandler<>() {
                    @Override
                    public ServerCall.Listener<byte[]> startCall(ServerCall<byte[], byte[]> call, Metadata headers) {
                        call.request(1); // Request message from client
                        return new ServerCall.Listener<>() {
                            byte[] image;

                            @Override
                            public void onMessage(byte[] message) {
                                image = message;
                            }

                            @Override
                            public void onHalfClose() {
                                System.out.println("gRPC server received: " + image.length + " bytes");
                                call.sendHeaders(new Metadata());
                                call.sendMessage("OK".getBytes());
                                call.close(Status.OK, new Metadata());
                            }

                            @Override
                            public void onCancel() {
                                System.err.println("gRPC call was cancelled");
                            }

                            @Override
                            public void onComplete() {
                                System.out.println("gRPC call completed");
                            }
                        };
                    }
                })
                .build();

        Server server = Grpc.newServerBuilderForPort(GrcpImageConfig.PORT, InsecureServerCredentials.create())
                .addService(service)
                .build()
                .start();

        System.out.println("gRPC server listening on port 6000");
        server.awaitTermination();
    }
}