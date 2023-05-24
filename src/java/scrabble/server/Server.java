package scrabble.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Server {
    private static final int PORT = 8080;
    private static final int GAME_ID_LENGTH = 6;

    private static final HashMap<String, Void> games = new HashMap<>();
    private static final HashMap<SocketChannel, ClientInfo> clients = new HashMap<>();
    private static final Random random = new Random();

    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    public static String generateGameID() {
        while(true) {
            var chars = new char[GAME_ID_LENGTH];

            for(int i = 0; i < chars.length; i++) {
                chars[i] = (char) ('a' + random.nextInt(26));
            }

            var id = new String(chars);

            if(!games.containsKey(id)) {
                return id;
            }
        }
    }

    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started on port " + PORT);

            while(true) {
                int readyChannels = selector.select();

                if(readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (!key.isAcceptable()) continue; // nesting = gross

                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);

                    clients.put(clientChannel, new ClientInfo(ClientState.CONNECTED));

                    System.out.println("New client connected: " + clientChannel.getRemoteAddress());

                    if (!key.isReadable()) continue;

                    try { //
                        clientChannel = (SocketChannel) key.channel();
                        readFromClient(clientChannel);
                    } catch(IOException e) {
                        System.out.println("Client disconnected");
                        key.cancel();
                        e.printStackTrace();
                    }

                    // Remove the processed key from the iterator
                    keyIterator.remove();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void readFromClient(SocketChannel clientChannel) throws IOException {
        System.out.println("Reading message...");
        lengthBuffer.clear();
        int bytesRead = clientChannel.read(lengthBuffer);

        if (bytesRead == -1) {
            // Connection closed by client
            clientChannel.close();
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            return;
        }

        if (lengthBuffer.position() == Integer.BYTES) {
            lengthBuffer.flip();
            int messageSize = lengthBuffer.getInt();

            // TODO: Maybe ensure that messageSize is less than a certain value
            ByteBuffer buffer = ByteBuffer.allocate(messageSize);
            clientChannel.read(buffer);
            System.out.println(new String(buffer.array()));
        }

//        var message = new String(data);

//        System.out.println("Received from client " + clientChannel.getRemoteAddress() + ": " + message);

        // Echo the message back to the client
//        clientChannel.write(ByteBuffer.wrap(data));
    }

    private static void broadcast(String msg) {

    }
}
