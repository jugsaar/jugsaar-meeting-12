package jugsaar12.nio.networking;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class D_NioMultiThreadedEchoServer {

    public static void main(String[] args) throws Exception {

        ExecutorService es = Executors.newCachedThreadPool();

        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {

            ssc.bind(new InetSocketAddress("localhost", 1337));

            while (true) {

                SocketChannel sc = ssc.accept(); //blocking call
                es.submit(() -> Util.process(sc));
            }
        }
    }
}
