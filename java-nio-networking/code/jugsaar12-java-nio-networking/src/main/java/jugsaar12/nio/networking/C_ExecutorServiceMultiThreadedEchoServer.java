package jugsaar12.nio.networking;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * TODO MAX_POOL_SIZE to 1000
 *
 * TODO show RejectedExecutionHandler -> Default AbortPolicy, CallerRunsPolicy
 */
public class C_ExecutorServiceMultiThreadedEchoServer {

    public static void main(String[] args) throws Exception {

		System.out.println("C_ExecutorServiceMultiThreadedEchoServer running");

		ExecutorService es = Executors.newCachedThreadPool();

        try (ServerSocket ss = new ServerSocket(1337)) {

            while (true) {

                Socket s = ss.accept(); // blocking-call, never returns null!
                es.submit(() -> Util.process(s));
            }
        }
    }
}