package jugsaar12.nio.networking;

import java.io.IOException;
import java.net.Socket;

/**
 * TODO (OSX) adjust sysctl kern.num_threads
 */
public class SocketGrabber {

  public static void main(String[] args) throws Exception {

    int socketCount = 10000;
    for (int i = 0; i < socketCount; i++) {
      try (Socket socket = new Socket("localhost", 1337)) {

        //allow reuse socket address and ensure that the socket doesn't stay around for too long.
        socket.setReuseAddress(true);
        socket.setSoLinger(true, 0);

        socket.getOutputStream().write('H');
        socket.getInputStream().read();

        System.out.printf("Socket: %s%n", i);
      } catch (IOException e) {
        System.err.printf("Could not connect - %s%n", e);
      }
    }
  }
}