package jugsaar12.nio.networking;

import java.io.IOException;
import java.net.Socket;

/**
 * Author: Tom
 */
public class SocketGrabber {

    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 10000; i++) {
            try {
                new Socket("localhost", 1337).getOutputStream().write('H');
                System.out.printf("Socket: %s%n", i);
            } catch (IOException e) {
                System.err.printf("Could not connect - %s%n", e);
            }
        }
    }
}
