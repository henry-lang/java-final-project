package scrabble;

import processing.core.PApplet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Scrabble extends PApplet {
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Dictionary dictionary;
    private static Board board;

    public static Dictionary getDictionary() {
        return dictionary;
    }

    public static void main(String[] args) {
        try {
            // Connect to the server
            var socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            // Read events from the console and send them to the server
            var scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter an event to send (or 'quit' to exit): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("quit")) {
                    break;
                }

                // Send the event to the server
                var buffer = ByteBuffer.wrap(input.getBytes());
                socketChannel.write(buffer);

                // Clear the buffer for the next event
                buffer.clear();
            }

            // Close the connection
            socketChannel.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        PApplet.main(Scrabble.class.getName(), args);
    }

    @Override
    public void settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT);
        pixelDensity(displayDensity());
    }

    @Override
    public void setup() {
        board = new Board();

        // Load the dictionary
        try {
            dictionary = Dictionary.loadFromFile("/dictionary.txt");
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't load dictionary. Exiting!");
            System.exit(1);
        }
    }

    @Override
    public void draw() {
        board.draw(this.g);
    }
}
