package jugsaar12.nio.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class F_NonBlockingSingleThreadedSelectorServer {

	public static void main(String[] args) throws Exception {

		System.out.println("F_NonBlockingSingleThreadedSelectorServer running");

		try (ServerSocketChannel ssc = ServerSocketChannel.open()) {

			ssc.bind(new InetSocketAddress("localhost", 1337));
			ssc.configureBlocking(false);

			//Use selector indirection instead of polling manually
			Selector selector = Selector.open();

			ssc.register(selector, SelectionKey.OP_ACCEPT);

			Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite = new ConcurrentHashMap<>();

			while (true) {

                /*
				* Selects a set of keys whose corresponding channels are ready for I/O operations.
                * It returns only after at least one channel is selected,this selector's {@link #wakeup wakeup}
                * method is invoked, or the current thread is interrupted, whichever comes first.
                 */
				selector.select(); //some more variants of select operation available

				for (Iterator<SelectionKey> itr = selector.selectedKeys().iterator(); itr.hasNext(); ) {

					SelectionKey key = itr.next();

					//remove current selectionKey since we handle it now...
					itr.remove();

					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) { // someone connected to our ServerSocketChannel
						accept(key, pendingDataToWrite);
					} else if (key.isWritable()) { //we can write data to the channel
						write(key, pendingDataToWrite);
					} else if (key.isReadable()) { //someone sent data to us
						read(key, pendingDataToWrite);
					}
				}
			}
		}
	}

	private static void accept(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite) throws Exception {

		//can only be a ServerSocketChannel
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

		SocketChannel sc = ssc.accept(); // non blocking never null
		System.out.printf("Connection from: %s%n", sc);

		sc.configureBlocking(false);
		sc.register(key.selector(), SelectionKey.OP_READ);

		pendingDataToWrite.put(sc, new ConcurrentLinkedQueue<>());
	}

	private static void write(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite) {

		SocketChannel chan = (SocketChannel) key.channel();
		Queue<ByteBuffer> queue = pendingDataToWrite.get(chan);

		try {

			ByteBuffer buf;
			while ((buf = queue.peek()) != null) {

				chan.write(buf); //write buffer to the channel

				if (!buf.hasRemaining()) { //buffer is completly written out
					queue.poll(); //remove buffer
				}
//				else {
//                    break; //
//                }
			}

			chan.register(key.selector(), SelectionKey.OP_READ);
		} catch (IOException e) {
			System.err.printf("Connection problem: %s%n", e.getMessage());
			key.cancel();
			pendingDataToWrite.remove(chan);
		}
	}

	private static void read(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite) {

		SocketChannel chan = (SocketChannel) key.channel();
		ByteBuffer buf = ByteBuffer.allocate(1024);

		try {

			int read = chan.read(buf);
			if (read == -1) {
				System.out.printf("Removing: %s%n", chan);
				chan.close();
				key.cancel();
				pendingDataToWrite.remove(chan);
				return;
			}

			buf.flip();
			for (int i = 0; i < buf.limit(); i++) {
				buf.put(i, (byte) Util.invertCharacterCase(buf.get()));
			}
			buf.flip();

			System.out.printf("Buffer: %s%n", buf);
			pendingDataToWrite.get(chan).add(buf);

			chan.register(key.selector(), SelectionKey.OP_WRITE);
			System.out.println("finished reading");
		} catch (IOException e) {
			System.err.printf("Connection problem: %s%n", e.getMessage());
			pendingDataToWrite.remove(chan);
			key.cancel();
		}
	}
}