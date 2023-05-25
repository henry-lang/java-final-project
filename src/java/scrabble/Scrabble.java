package scrabble;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

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

    private static ArrayBlockingQueue queue;

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
            queue = new ArrayBlockingQueue(1024);
            System.out.println("Connected to the server.");

            // Read events from the console and send them to the server
            var scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter a username: ");
                String input = "logon:" + scanner.nextLine();
                send(input);
                break;
                // TODO: add method for receiving server msgs
            }

            while (true) {
                System.out.print("Enter an event to send (or 'quit' to exit): ");
                var input = scanner.nextLine();

                if (input.equalsIgnoreCase("quit")) {
                    break;
                }

                send(input);
//                if (queue.
            }

            // Close the connection
            socketChannel.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        PApplet.main(Scrabble.class.getName(), args);
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

    @Override
    public void settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT);
        int displayDensity = displayDensity();
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    @Override
    public void setup() {
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
    }
}
