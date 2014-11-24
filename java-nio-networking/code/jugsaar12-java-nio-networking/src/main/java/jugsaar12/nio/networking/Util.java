package jugsaar12.nio.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Util {

	public static int invertCharacterCase(int data) {
		return Character.isLetter(data) ? data ^ ' ' : data;
	}

	public static void process(Socket s) {
		
		try (InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
		
			int data;
			while ((data = in.read()) != -1) {
				data = Util.invertCharacterCase(data);
				out.write(data);
			}
			
		} catch (IOException e) {
			System.err.println("Connection problem: " + e.getMessage());
		}
	}

	public static void process(SocketChannel sc) {

		System.out.println("Connection from: " + sc);

		try {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			// position = 0
			// limit == capacity == 1024

			while (sc.read(buf) != -1) {
				// Util.transmogrify(data);
				// bytebuffer (after hello world received)
				// position = 11
				// limit == 1024
				// capacity == 1024

				// buf.limit(buf.position()).position(0);

				// set position to 0 limit to old position
				buf.flip();
				for (int i = 0; i < buf.limit(); i++) {
					buf.put(i, (byte) Util.invertCharacterCase(buf.get()));
				}
				buf.flip();

				System.out.printf("Buffer: %s%n", buf);

				int bytesWritten = sc.write(buf);
				System.out.printf("Wrote: %s bytes%n", bytesWritten);
				
				buf.clear();
			}

			System.out.println("finished reading");
		} catch (IOException e) {
			System.err.println("Connection problem: " + e.getMessage());
		}
	}
}
