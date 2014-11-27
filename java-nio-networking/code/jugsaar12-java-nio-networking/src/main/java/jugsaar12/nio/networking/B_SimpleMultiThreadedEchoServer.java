package jugsaar12.nio.networking;

import java.net.ServerSocket;
import java.net.Socket;

public class B_SimpleMultiThreadedEchoServer {

    public static void main(String[] args) throws Exception {

		System.out.println("B_SimpleMultiThreadedEchoServer running");

        try (ServerSocket ss = new ServerSocket(1337)) {

            while (true) {

                Socket s = ss.accept(); // blocking-call, never returns null!
                new Thread(() -> Util.process(s)).start();
            }
        }
    }
}