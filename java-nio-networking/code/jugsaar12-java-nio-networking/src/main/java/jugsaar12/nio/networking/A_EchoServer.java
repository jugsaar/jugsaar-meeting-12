package jugsaar12.nio.networking;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO explain TCP-Backlog
 */
public class A_EchoServer {

  public static void main(String[] args) throws Exception {

    System.out.println("A_EchoServer running");

    try (ServerSocket ss = new ServerSocket(1337)) {

      while (true) {

        Socket s = ss.accept(); // blocking-call, never returns null!
        Util.process(s);
      }
    }
  }
}
