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

    System.out.printf("Connection from: %s%n", s);

    try (InputStream in = s.getInputStream(); //
         OutputStream out = s.getOutputStream()) {

      System.out.printf("Start reading from: %s%n", s);

      int data;
      while ((data = in.read()) != -1) {
        data = Util.invertCharacterCase(data);
        out.write(data);
      }

      System.out.printf("Finished reading from: %s%n", s);
    } catch (IOException e) {
      System.err.printf("Connection problem: %s%n", e.getMessage());
    }
  }

  /**
   * ByteBuffer -> Byte array + additional information:
   * <pre>
   *
   * - Position - where are we in the buffer
   * - limit -
   * - capacity -
   * Ex.
   * "Hello World" - 11 characters
   *
   * ByteBuffer buf = ByteBuffer.allocate(1024);
   *
   * First constructed:
   * - Position = 0
   * - Limit = Capacity = 1024
   *
   * After Hello world
   * - Position = 11
   * - Limit = 1024
   * - Capacity = 1024
   *
   * We want to set position = 0 and limit = 11
   * Variant 1)
   * buffer.limit(buf.position())
   * buf.pos(0)
   *
   * Variant 2)
   * buffer.limit(buf.position()).pos(0)
   *
   * Variant 3)
   * buffer.flip()
   *
   * <pre>
   */
  public static void process(SocketChannel sc) {

    System.out.printf("Connection from: %s%n", sc);

    try {
      //two choices: allocate -> HeapByteBuffer, allocateDirect -> DirectByteBuffer (might end up in native memory off-heap!)
      ByteBuffer buf = ByteBuffer.allocate(1024);
      // position = 0
      // limit == capacity == 1024

      while (sc.read(buf) != -1) { //-1 -> EOF / channel was closed

        // set position to 0 and limit to old position
        buf.flip();
        for (int i = 0; i < buf.limit(); i++) {
          buf.put(i, (byte) invertCharacterCase(buf.get()));
        }
        buf.flip();

        System.out.printf("Buffer: %s%n", buf);

        int bytesWritten = sc.write(buf);
        System.out.printf("Wrote: %s bytes%n", bytesWritten);

        //clear it for reuse
        buf.clear();
      }

      System.out.println("finished reading");
    } catch (IOException e) {
      System.err.printf("Connection problem: %s%n", e.getMessage());
    }
  }
}