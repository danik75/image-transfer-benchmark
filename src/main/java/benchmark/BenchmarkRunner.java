// BenchmarkRunner.java
package benchmark;

import grcp.GrpcImageClient;
import grcp.GrpcImageServer;
import rudp.RudpReceiver;
import rudp.RudpSender;
import tcp.TcpImageClient;
import tcp.TcpImageServer;
import udp.UdpImageClient;
import udp.UdpImageServer;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {
    private static final int RUNS = 100;

    public static void main(String[] args) throws Exception {
        String imagePath = "test-image.jpg"; // Place this image in the root of the project

        startServers();

        System.out.println("Running benchmarks...");

        List<Long> tcpTimes = runBenchmark("TCP", () -> TcpImageClient.send(imagePath));
        List<Long> grpcTimes = runBenchmark("gRPC", () -> GrpcImageClient.send(imagePath));
        List<Long> udpTimes = runBenchmark("UDP", () -> UdpImageClient.send(imagePath));
        List<Long> rudpTimes = runBenchmark("RUDP", () -> new RudpSender().send(imagePath));

        long tcpTotal = tcpTimes.stream().mapToLong(Long::longValue).sum();
        long grpcTotal = grpcTimes.stream().mapToLong(Long::longValue).sum();
        long udpTotal = udpTimes.stream().mapToLong(Long::longValue).sum();
        long urdpTotal = rudpTimes.stream().mapToLong(Long::longValue).sum();

        double tcpAvg = tcpTotal / (double) RUNS;
        double grpcAvg = grpcTotal / (double) RUNS;
        double udpAvg = udpTotal / (double) RUNS;
        double urdpAvg = urdpTotal / (double) RUNS;


        System.out.println("\nAggregated Benchmark Results:");
        System.out.println("+-------+----------+----------+----------+----------+");
        System.out.println("| RUN   | TCP (ms) | gRPC (ms)| UDP (ms) | RUDP (ms)|");
        System.out.println("+-------+----------+----------+----------+----------+");

        for (int i = 0; i < RUNS; i++) {
            System.out.printf("| %5d | %8d | %8d | %8d | %8d |%n",
                    i + 1,
                    tcpTimes.get(i),
                    grpcTimes.get(i),
                    udpTimes.get(i),
                    rudpTimes.get(i));
        }

        System.out.println("+-------+----------+----------+----------+----------+");
        System.out.printf("| TOTAL | %8d | %8d | %8d | %8d |%n", tcpTotal, grpcTotal, udpTotal, urdpTotal);
        System.out.printf("| AVG   | %8.2f | %8.2f | %8.2f | %8.2f |%n", tcpAvg, grpcAvg, udpAvg, urdpAvg);
        System.out.println("+-------+----------+----------+----------+----------+");
    }

    private static void startServers() {
        // Start TCP server
        Thread tcpThread = new Thread(() -> {
            try {
                TcpImageServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("TCP Server error: " + e.getMessage());
            }
        });
        tcpThread.setDaemon(true); // Mark as daemon
        tcpThread.start();

        // Start gRPC server
        Thread grpcThread = new Thread(() -> {
            try {
                GrpcImageServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("gRPC Server error: " + e.getMessage());
            }
        });
        grpcThread.setDaemon(true); // Mark as daemon
        grpcThread.start();

        // Start UDP server
        Thread udpThread = new Thread(() -> {
            try {
                UdpImageServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("UDP Server error: " + e.getMessage());
            }
        });
        udpThread.setDaemon(true); // Mark as daemon
        udpThread.start();

        // Start RUDP server
        Thread rudpThread = new Thread(() -> {
            try {
                RudpReceiver.main(new String[]{});
            } catch (Exception e) {
                System.err.println("RUDP Server error: " + e.getMessage());
            }
        });
        rudpThread.setDaemon(true); // Mark as daemon
        rudpThread.start();
    }

    private static List<Long> runBenchmark(String protocol, BenchmarkTask task) throws Exception {

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            long time = task.execute();
            times.add(time);
        }
        return times;
    }


    @FunctionalInterface
    interface BenchmarkTask {
        long execute() throws Exception;
    }
}