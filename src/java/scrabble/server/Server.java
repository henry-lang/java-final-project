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

public class Server {
    private static final int PORT = 8080;
    private static final int GAME_ID_LENGTH = 6;

    private static final HashMap<String, GameInfo> games = new HashMap<>();
    private static SocketChannel randomWaiting;
    private static final HashMap<SocketChannel, ClientInfo> clients = new HashMap<>();
    private static final Random random = new Random();

    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    public static String generateGameID() {
        while(true) {
            char[] chars = new char[GAME_ID_LENGTH];

            for(int i = 0; i < chars.length; i++) {
                chars[i] = (char) ('a' + random.nextInt(26));
            }

            String id = new String(chars);

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

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

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
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        try {
                            readFromClient(clientChannel);
                        } catch(IOException e) {
                            System.out.println("Client disconnected");
                            if(clientChannel.equals(randomWaiting)) {
                                randomWaiting = null;
                            }
                            key.cancel();
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
            clients.remove(clientChannel);
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
        for (String gameID : games.keySet()) {
            if (gameID.equals(id)) {
                return games.get(gameID).maxPlayers;
            }
        }
        return false;
    }

    private static void parseMessage(String msg, SocketChannel client) {
        String[] split = msg.split(":");
        String res = null;
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
                ClientInfo info = clients.get(client);
                if(info.state != ClientState.CONNECTED) {
                    res = "create_fail:already in game or waiting";
                    break;
                }
                String id = generateGameID();
                games.put(id, new GameInfo(id));
                info.state = ClientState.IN_GAME;
                info.gameID = id;
                res = "create_success:" + id;
                break;
            }

            case "random": {
                ClientInfo info = clients.get(client);
                if(info.state != ClientState.CONNECTED) {
                    res = "random_fail:already in game or waiting";
                    break;
                }
                if(randomWaiting == null) {
                    randomWaiting = client;
                    clients.get(randomWaiting).state = ClientState.IN_GAME;
                    res = "random_waiting";
                    System.out.println("New client waiting for random game");
                } else {
                    String id = generateGameID();
                    games.put(id, new GameInfo(id));
                    send(randomWaiting, "random_game_found");
                    res = "random_game_found";
                }
                break;
            }

            case "random_cancel": {
                if(client.equals(randomWaiting)) {
                    randomWaiting = null;
                    clients.get(client).state = ClientState.CONNECTED;
                }
                break;
            }

            case "join": {
                // if (idNonexistent(split[1])) res = "join_fail:id nonexistent";
                // else if (gameInProgress(split[1])) res = "join_fail:game in progress";
//                System.out.println(clients.get(client).state + " " + clients.get(client).state.equals(ClientState.IN_GAME));
                ClientInfo info = clients.get(client);
                if (split.length == 1) res = "join_fail:no join code";
                else if (info.state.equals(ClientState.IN_GAME)) res = "join_fail:in active game";
                else if (invalidGameReq(split[1])) res = "join_fail:bad game request";
                else {
                    games.get(split[1]).addPlayer();
                    info.state = ClientState.IN_GAME;
                    info.gameID = split[1];
                    res = "join_success";
                }
                break;
            }

            case "leave": {
                ClientInfo info = clients.get(client);
                if (!info.state.equals(ClientState.IN_GAME)) res = "leave_fail:not in active game";
                else {
                    games.get(info.gameID).removePlayer();
                    info.gameID = "";
                    info.state = ClientState.CONNECTED;
                    res = "leave_success";
                }
                break;
            }

            case "turn": {
                // TODO: come back to this one bc it's much more complicated
                res = "turn_fail:not implemented";
                break;
            }

            default: {
                res = "msg_fail:unknown request";
                break;
            }
        }

        if(res != null) {
            send(client, res);
        }
    }

    private static void send(SocketChannel client, String msg) {
        try {
            byte[] bytes = msg.getBytes();
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
        for (SocketChannel client : clients.keySet()) send(client, msg);
    }
}
