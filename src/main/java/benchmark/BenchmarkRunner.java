// BenchmarkRunner.java
package benchmark;

import grcp.GrpcImageClient;
import rudp.RudpConfig;
import rudp.RudpSender;
import tcp.TcpImageClient;
import udp.UdpImageClient;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {
    private static final int RUNS = 10;

    public static void main(String[] args) throws Exception {
        String imagePath = "test-image.jpg"; // Place this image in the root of the project

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