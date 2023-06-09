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
    // The port to run the server on
    private static final int PORT = 8080;

    // The length of the generated game ids
    private static final int GAME_ID_LENGTH = 6;

    // All the games that are currently running: gameID -> info about the game
    private static final HashMap<String, GameInfo> games = new HashMap<>();

    // The client who is currently waiting for someone else to want to play
    private static SocketChannel randomWaiting;

    // All the clients that are currently connected, corresponding to info about them (their state)
    private static final HashMap<SocketChannel, ClientInfo> clients = new HashMap<>();

    // The server's random number generator
    private static final Random random = new Random();

    // Similarly to the client, this avoids a temporary allocation by reusing the same buffer for the length integer
    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    // Generate a random GAME_ID_LENGTH game id that isn't in use
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
            // Boilerplate server initialization code
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started on port " + PORT);

            while(true) {
                int readyChannels = selector.select();

                if(readyChannels == 0) continue;

                // Get all the clients that need processing
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    // A client has just connected, and we need to initialize them
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);

                        // Give them initial state in the clients map
                        clients.put(clientChannel, new ClientInfo(ClientState.CONNECTED));

                        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    }
                    // A client has sent data
                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        try {
                            // Try to read data from the client
                            readFromClient(clientChannel);
                        } catch(IOException e) {
                            // The client has disconnected
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

    // Read a message from the client and handle it
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

        // If we read the length successfully, get the message into a String
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
    }

    // Parse the message that the user sent and actually handle it
    private static void parseMessage(String msg, SocketChannel client) {
        // The parts of the message, separated by :
        String[] split = msg.split(":");
        // The optional response to send back to the client
        String res = null;
        switch (split[0]) { // Refer to documentation for message parsing.
            // Player wants to join a random game
            case "random": {
                ClientInfo info = clients.get(client);
                if(info.state != ClientState.CONNECTED) {
                    res = "random_fail:already in game or waiting";
                    break;
                }
                info.username = split[1];
                if(randomWaiting == null) {
                    randomWaiting = client;
                    clients.get(randomWaiting).state = ClientState.IN_GAME;
                    res = "random_waiting";
                    System.out.println("New client waiting for random game");
                } else {
                    String id = generateGameID();
                    GameInfo gameInfo = new GameInfo(id, randomWaiting, client);
                    games.put(id, gameInfo);
                    clients.get(randomWaiting).gameID = id;
                    info.gameID = id;
                    info.state = ClientState.IN_GAME;
                    // random_game_start:{username}:{tiles}:{their_turn}
                    send(randomWaiting, "random_game_start:" + info.username + ":" + gameInfo.getTileMessage(7) + ":true");
                    res = "random_game_start:" + clients.get(randomWaiting).username + ":" + gameInfo.getTileMessage(7) + ":false";
                    randomWaiting = null;
                }
                break;
            }

            // Player wants to cancel their waiting in the random queue
            case "random_cancel": {
                if(client.equals(randomWaiting)) {
                    randomWaiting = null;
                    clients.get(client).state = ClientState.CONNECTED;
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
                ClientInfo info = clients.get(client);
                if(info.state != ClientState.IN_GAME) {
                    res = "turn_fail:not in active game";
                    break;
                }

                int numTiles = split.length - 2;
                GameInfo game = games.get(info.gameID);
                res = "turn_success:" + game.getTileMessage(numTiles);
                int start = msg.indexOf(':');
                send(game.getOpponent(client), "opponent_turn:" + msg.substring(start + 1));

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
}
