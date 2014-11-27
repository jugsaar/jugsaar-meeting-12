package jugsaar12.nio.networking;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO inline newChachedThreadPool
 * TODO set MAX_POOL_SIZE to 1000
 * TODO change to fixedThreadPool
 */
public class D_NioMultiThreadedEchoServer {

  public static void main(String[] args) throws Exception {

    System.out.println("D_NioMultiThreadedEchoServer running");

    ExecutorService es = Executors.newCachedThreadPool();

    //Difference: Instead of using a ServerSocket we use a ServerSocketChannel
    try (ServerSocketChannel ssc = ServerSocketChannel.open()) {

      //bind it to an network interface associated with the given InetSocketAddress
      ssc.bind(new InetSocketAddress("localhost", 1337));

      while (true) {

        //Instead of a Socket, we get a SocketChannel
        SocketChannel sc = ssc.accept(); //blocking call - never null

        es.submit(() -> Util.process(sc));
      }
    }
  }
}