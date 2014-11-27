package jugsaar12.nio.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class E_NonBlockingSingleThreadedPollingServer {

	public static void main(String[] args) throws Exception {

		System.out.println("E_NonBlockingSingleThreadedPollingServer running");

		try (ServerSocketChannel ssc = ServerSocketChannel.open()) {

			ssc.bind(new InetSocketAddress("localhost", 1337));
			ssc.configureBlocking(false);

			Set<SocketChannel> sockets = new HashSet<>();
			ByteBuffer buf = ByteBuffer.allocate(1024);

			while (true) {

				//polling
				tryRegisterNewSocketChannel(ssc, sockets);

				tryReadFromAndWriteToAnySocketChannel(sockets, buf);
			}
		}
	}

	private static void tryRegisterNewSocketChannel(ServerSocketChannel ssc, Set<SocketChannel> sockets) throws IOException {

		SocketChannel sc = ssc.accept(); // non-blocking call, may be null

		if (sc == null) {
			return;
		}

		System.out.printf("Connection from: %s%n", sc);
		sc.configureBlocking(false);
		sockets.add(sc);
	}

	private static void tryReadFromAndWriteToAnySocketChannel(Set<SocketChannel> sockets, ByteBuffer buf) {

		for (Iterator<SocketChannel> iter = sockets.iterator(); iter.hasNext(); ) {

			SocketChannel chan = iter.next();

			try {

				int read = chan.read(buf);
				if (read == -1) {

					iter.remove();
					System.out.printf("Removed connection: %s%n", chan);
				} else if (read != 0) {
					//Non-blocking -> read will be 0 most of the time...

					buf.flip();
					for (int i = 0; i < buf.limit(); i++) {
						buf.put(i, (byte) Util.invertCharacterCase(buf.get()));
					}

					buf.flip();
					System.out.printf("Buffer: %s%n", buf);

					int bytesWritten = chan.write(buf);
					System.out.printf("Wrote: %s bytes%n", bytesWritten);

					buf.clear();
					System.out.println("finished reading");
				}
			} catch (IOException e) {
				System.err.printf("Connection problem: %s%n", e.getMessage());
				iter.remove();
			}
		}
	}
}