package scrabble;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.logging.Logger;

public class Scrabble extends PApplet {
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Dictionary dictionary;
    private static Board board;
    private static Tile[] rack;

    public static Dictionary getDictionary() {
        return dictionary;
    }

    public static void main(String[] args) {
//        try {
//            // Connect to the server
//            var socketChannel = SocketChannel.open();
//            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
//            System.out.println("Connected to the server.");
//
//            // Read events from the console and send them to the server
//            var scanner = new Scanner(System.in);
//            while (true) {
//                System.out.print("Enter an event to send (or 'quit' to exit): ");
//                var input = scanner.nextLine();
//                var bytes = input.getBytes();
//
//                if (input.equalsIgnoreCase("quit")) {
//                    break;
//                }
//
//                // Send the event to the server
//                var buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
//                buffer.putInt(bytes.length);
//                buffer.put(bytes);
//                buffer.flip();
//                socketChannel.write(buffer);
//            }
//
//            // Close the connection
//            socketChannel.close();
//            System.out.println("Connection closed.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        PApplet.main(Scrabble.class.getName(), args);
    }

    @Override
    public void settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT);
        var displayDensity = displayDensity();
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    @Override
    public void setup() {
        board = new Board();

        // Load the dictionary
        try {
            dictionary = Dictionary.loadFromFile("/dictionary.txt");
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load dictionary. Exiting!");
            System.exit(1);
        }

        System.out.println("Loaded dictionary with " + dictionary.size() + " words");

        var placement = board.checkWordPlacement();
        if(placement.isValid) {
            placement.words.forEach(p -> System.out.println(p.pointValue + " " + p.word));
        } else {
            System.out.println(placement.invalidReason);
        }
    }

    @Override
    public void draw() {
        board.draw(this.g);
    }
}
