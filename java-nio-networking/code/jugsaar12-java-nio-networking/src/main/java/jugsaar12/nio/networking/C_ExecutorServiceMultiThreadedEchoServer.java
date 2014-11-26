package jugsaar12.nio.networking;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class C_ExecutorServiceMultiThreadedEchoServer {

    public static void main(String[] args) throws Exception {

        ExecutorService es = Executors.newCachedThreadPool();

        try (ServerSocket ss = new ServerSocket(1337)) {

            while (true) {

                Socket s = ss.accept();
                es.submit(() -> Util.process(s));
            }
        }
    }
}
