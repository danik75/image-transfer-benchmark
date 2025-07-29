package tcp;

import java.io.*;
import java.net.*;

public class TcpImageServer {
    public static void main(String[] args) {
        int port = TcpImageConfig.PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Server is listening on port " + port);

            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream()
                ) {
                    System.out.println("Accepted connection from " + socket.getInetAddress());

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[TcpImageConfig.BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(data)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }

                    System.out.println("Received " + buffer.size() + " bytes from client.");

                    // Optionally save the image:
                    try (FileOutputStream fos = new FileOutputStream("tcp_received_image")) {
                        fos.write(buffer.toByteArray());
                    }

                    // Send acknowledgment
                    out.write(1);
                    out.flush();
                    System.out.println("Acknowledgment sent.\n");

                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}