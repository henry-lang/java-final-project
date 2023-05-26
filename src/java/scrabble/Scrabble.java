package scrabble;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Scrabble extends PApplet {
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static Random random;
    private static Dictionary dictionary;
    private static Board board;
    private static TileRack rack;
    private static SocketChannel socketChannel;
    private static ConcurrentLinkedQueue<String> queue;
    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    public static Random getRandom() {
        return random;
    }

    public static Dictionary getDictionary() {
        return dictionary;
    }

    public static void main(String[] args) {
        try {
            // Connect to the server
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            queue = new ConcurrentLinkedQueue();
            System.out.println("Connected to the server.");

            // Read events from the console and send them to the server
            startReception();
            openCmdThread();
            PApplet.main(Scrabble.class.getName(), args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openCmdThread() { // to be used for development
        new Thread(new Runnable() {
            public void run() {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter a username: ");
                String input = "logon:" + scanner.nextLine();
                send(input);
                while (true) {
                    System.out.print("Enter an event to send (or 'quit' to exit): ");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("quit")) {
                        System.exit(0);
                    }

                    send(input);
                }
            }
        }).start();
    }

    public static void startReception() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        lengthBuffer.clear();
                        int bytesRead = socketChannel.read(lengthBuffer);
                        if (bytesRead == -1) break; // Connection closed by server

                        if (lengthBuffer.position() == Integer.BYTES) { // copied from server
                            lengthBuffer.flip();
                            int messageSize = lengthBuffer.getInt();
                            ByteBuffer buffer = ByteBuffer.allocate(messageSize);
                            socketChannel.read(buffer);
                            String msg = new String(buffer.array());

                            queue.offer(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("bruv k bye");
                    System.exit(0);
                }
            }
        }).start();
    }

    public static void parseMessage(String res) {
        String[] split = res.split(":");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        String msg = formatter.format(time) + ": ";
        switch (split[0]) {
            case "logon_success": {
                msg += "Successfully logged on with username.";
                break;
            }

            case "logon_fail": {
                msg += "Failed to log on. Reason: " + split[1];
                break;
            }

            case "create_success": {
                msg += "Successfully created game. ID: " + split[1];
                break;
            }

            case "create_fail": {
                msg += "Failed to create game. Reason: " + split[1];
                break;
            }

            case "join_success": {
                msg += "Successfully joined game.";
                break;
            }

            case "join_fail": {
                msg += "Failed to join game. Reason: " + split[1];
                break;
            }

            case "leave_success": {
                msg += "Successfully left game.";
                break;
            }

            case "leave_fail": {
                msg += "Failed to leave game. Reason: " + split[1];
                break;
            }

            case "turn_success:": {
                msg += "Successful turn.";
                break;
            }

            case "turn_fail": {
                msg += "Turn submission failed. Reason: " + split[1];
                break;
            }

            case "ping": {
                msg += "[debug] received ping";
                break;
            }

            default: {
                msg += "Unknown response from server. Message received: " + res;
                break;
            }
        }

        System.out.println(msg);
    }

    public static void send(String msg) {
        try {
            byte[] bytes = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process() {
        while (!queue.isEmpty()) {
//            System.out.println("parsing");
            parseMessage(queue.poll());
        }
    }

    public void createExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Shutting down...");
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    public void settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT);
        int displayDensity = displayDensity();
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    @Override
    public void setup() {
        createExitHandler();
        windowTitle("Phrases with Phriends");

        random = new Random();
        board = new Board();
        rack = new TileRack();

        // Load the dictionary
        try {
            dictionary = Dictionary.loadFromFile("/dictionary.txt");
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load dictionary. Exiting!");
            System.exit(1);
        }

        System.out.println("Loaded dictionary with " + dictionary.size() + " words");

        WordPlacementInfo placement = board.checkWordPlacement();
        if(placement.isValid) {
            placement.words.forEach(p -> System.out.println(p.pointValue + " " + p.word));
        } else {
            System.out.println(placement.invalidReason);
        }
    }

    @Override
    public void draw() {
        board.draw(this.g);
        rack.draw(this.g);
        process();
    }
}
