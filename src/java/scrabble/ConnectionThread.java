package scrabble;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
public class ConnectionThread extends Thread {
    private SocketChannel socketChannel;
    private String ip;

    private ArrayBlockingQueue queue;

    public ConnectionThread(SocketChannel socketChannel, String ip, ArrayBlockingQueue queue) {
        this.socketChannel = socketChannel;
        this.ip = ip;
        this.queue = queue;
    }

//    public static void send(String msg) {
//        try {
//            byte[] bytes = msg.getBytes();
//            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
//            buffer.putInt(bytes.length);
//            buffer.put(bytes);
//            buffer.flip();
//            socketChannel.write(buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) break; // Connection closed by server
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                queue.put(new String(bytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
