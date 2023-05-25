package scrabble.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    private static final HashMap<GameInfo, Void> games = new HashMap<>();
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

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);

                        clients.put(clientChannel, new ClientInfo(ClientState.CONNECTED));

                        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    }
                    if (key.isReadable()) {
                        System.out.println("reading");
                        try {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            readFromClient(clientChannel);
                        } catch(IOException e) {
                            System.out.println("Client disconnected");
                            key.cancel();
                            e.printStackTrace();
                        }
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
            String msg = new String(buffer.array());
            System.out.println("Received message from " + clientChannel.getRemoteAddress() + ": " + msg);
            parseMessage(msg, clientChannel);
        }

//        var message = new String(data);

//        System.out.println("Received from client " + clientChannel.getRemoteAddress() + ": " + message);

        // Echo the message back to the client
//        clientChannel.write(ByteBuffer.wrap(data));
    }

    private static boolean isUsernameInUse(String username, SocketChannel client) {
        for (ClientInfo info: clients.values()) {
            if (!info.equals(clients.get(client)) && info.username != null && info.username.equals(username)) return true;
            //  ^^ not checking same client          ^^ username exists       ^^ username == username
        }
        return false;
    }

    private static boolean invalidGameReq(String id) {
        for (GameInfo game : games.keySet()) {
            System.out.println(game.id + " " + id + " " + game.id.equals(id) + " " + game.maxPlayers);
            if (!game.id.equals(id)) return true;
            if (game.maxPlayers) return true;
        }
        return false;
    }
    private static void parseMessage(String msg, SocketChannel client) {
        String[] split = msg.split(":");
        String res = "sample";
        switch (split[0]) { // Refer to documentation for message parsing.
            case "logon": {
                if (isUsernameInUse(split[1], client)) res = "logon_fail:username in use";
                else {
                    clients.get(client).username = split[1];
                    res = "logon_success";
                }
                break;
            }

            case "create": {
                // generate game id; return success
                // no reason for this to fail tbh
                try {
                    String id = generateGameID();
                    games.put(new GameInfo(id), null);
                    res = "create_success:" + id;
                } catch (Exception e) {
                    res = "create_fail:" + e.getMessage();
                }
                break;
            }

            case "join": {
                // if (idNonexistent(split[1])) res = "join_fail:id nonexistent";
                // else if (gameInProgress(split[1])) res = "join_fail:game in progress";
                if (invalidGameReq(split[1])) res = "join_fail:bad game request";
                else res = "join_success";
                break;
            }

            case "turn": {
                // TODO: come back to this one bc it's much more complicated
                break;
            }

            default: {
                res = "Unknown message.";
                break;
            }
        }

        System.out.println(res);
        try {
            byte[] bytes = res.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcast(String msg) {

    }
}
