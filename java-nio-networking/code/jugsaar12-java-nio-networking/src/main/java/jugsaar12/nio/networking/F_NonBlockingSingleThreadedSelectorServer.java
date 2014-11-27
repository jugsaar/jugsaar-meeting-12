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

            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            Map<SocketChannel, Queue<ByteBuffer>> pendingData = new ConcurrentHashMap<>();

            while (true) {

                /*
                * Selects a set of keys whose corresponding channels are ready for I/O operations.
                * It returns only after at least one channel is selected,this selector's {@link #wakeup wakeup}
                * method is invoked, or the current thread is interrupted, whichever comes first.
                 */
                selector.select();

                for (Iterator<SelectionKey> itr = selector.selectedKeys().iterator(); itr.hasNext(); ) {

                    SelectionKey key = itr.next();
                    itr.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key, pendingData);
                    } else if (key.isWritable()) {
                        write(key, pendingData);
                    } else if (key.isReadable()) {
                        read(key, pendingData);
                    }
                }
            }
        }
    }

    private static void accept(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingData) throws Exception {

        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

        SocketChannel sc = ssc.accept(); // non blocking never null
        System.out.printf("Connection from: %s%n", sc);

        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ);

        pendingData.put(sc, new ConcurrentLinkedQueue<>());
    }

    private static void write(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingData) {

        SocketChannel chan = (SocketChannel) key.channel();
        Queue<ByteBuffer> queue = pendingData.get(chan);

        try {

            ByteBuffer buf;
            while ((buf = queue.peek()) != null) {
                chan.write(buf);
                if (!buf.hasRemaining()) {
                    queue.poll();
                } else {
                    break;
                }
            }

            chan.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            System.err.printf("Connection problem: %s%n", e.getMessage());
            key.cancel();
            pendingData.remove(chan);
        }
    }

    private static void read(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingData) {

        SocketChannel chan = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);

        try {

            int read = chan.read(buf);
            if (read == -1) {
                System.out.printf("Removing: %s%n", chan);
                chan.close();
                key.cancel();
                pendingData.remove(chan);
                return;
            }

            buf.flip();
            for (int i = 0; i < buf.limit(); i++) {
                buf.put(i, (byte) Util.invertCharacterCase(buf.get()));
            }
            buf.flip();

            System.out.printf("Buffer: %s%n", buf);
            pendingData.get(chan).add(buf);

            chan.register(key.selector(), SelectionKey.OP_WRITE);
            System.out.println("finished reading");

        } catch (IOException e) {
            System.err.printf("Connection problem: %s%n", e.getMessage());
            pendingData.remove(chan);
            key.cancel();
        }
    }
}