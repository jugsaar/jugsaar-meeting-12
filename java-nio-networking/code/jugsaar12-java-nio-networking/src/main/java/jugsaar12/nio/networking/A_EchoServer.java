package jugsaar12.nio.networking;

import java.net.ServerSocket;
import java.net.Socket;

public class A_EchoServer {

    public static void main(String[] args) throws Exception {

        try (ServerSocket ss = new ServerSocket(1337)) {

            while (true) {

                Socket s = ss.accept();
                Util.process(s);
            }
        }
    }
}
