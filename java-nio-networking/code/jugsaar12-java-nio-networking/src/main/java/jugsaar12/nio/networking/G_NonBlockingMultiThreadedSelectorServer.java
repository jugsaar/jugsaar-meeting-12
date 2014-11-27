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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class G_NonBlockingMultiThreadedSelectorServer {

    static Logger LOG = Logger.getLogger("server");

    public static void main(String[] args) throws Exception {

		System.out.println("G_NonBlockingMultiThreadedSelectorServer running");

        ExecutorService es = Executors.newFixedThreadPool(10);

        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {

            Selector selector = Selector.open();
            ssc.bind(new InetSocketAddress("localhost", 1337));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            Queue<SocketChannel> writeableChannels = new LinkedBlockingQueue<>();
            Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite = new ConcurrentHashMap<>();

            while (true) {

                selector.select();

                SocketChannel changeToWrite;
                while ((changeToWrite = writeableChannels.poll()) != null) {
                    changeToWrite.register(selector, SelectionKey.OP_WRITE);
                }

                for (Iterator<SelectionKey> itr = selector.selectedKeys().iterator(); itr.hasNext(); ) {

                    SelectionKey key = itr.next();
                    itr.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key, pendingDataToWrite);
                    } else if (key.isWritable()) {
                        write(key, pendingDataToWrite);
                    } else if (key.isReadable()) {
                        read(key, pendingDataToWrite, writeableChannels, es);
                    }
                }
            }
        }
    }


    static void accept(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToRead) throws IOException {

        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

        SocketChannel sc = ssc.accept(); // non blocking never null
        LOG.info("Connection from: " + sc);

        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ);

        pendingDataToRead.put(sc, new ConcurrentLinkedQueue<>());
    }

    static void write(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite) {

        SocketChannel chan = (SocketChannel) key.channel();
        Queue<ByteBuffer> queue = pendingDataToWrite.get(chan);

        try {
            ByteBuffer buf;
            while ((buf = queue.peek()) != null) {

				chan.write(buf); //write buffer to the channel

                if (!buf.hasRemaining()) {
                    queue.poll();
                } else {
                    break;
                }
            }

            chan.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            LOG.info("Connection problem: " + e.getMessage());
            pendingDataToWrite.remove(chan);
        }
    }

    static void read(SelectionKey key, Map<SocketChannel, Queue<ByteBuffer>> pendingDataToWrite, Queue<SocketChannel> writeableChannels, ExecutorService es) {

        SocketChannel chan = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {

            int read = chan.read(buf);
            if (read == -1) {
                LOG.info("Removing: " + chan);
                chan.close();
                pendingDataToWrite.remove(chan);
                key.cancel();
                return;
            }

            // Process asynchronously
            es.submit(() -> {
                buf.flip();
                for (int i = 0; i < buf.limit(); i++) {
                    buf.put(i, (byte) Util.invertCharacterCase(buf.get()));
                }
                buf.flip();
                LOG.info(String.format("Buffer: %s", buf));
				LOG.info("finished reading");

                pendingDataToWrite.get(chan).add(buf);

                writeableChannels.add(chan);

                //Signal that we are done reading asynchronously
                key.selector().wakeup();
            });

        } catch (IOException e) {
            LOG.warning("Connection problem: " + e.getMessage());
            pendingDataToWrite.remove(chan);
        }
    }
}