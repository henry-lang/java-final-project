package scrabble;

import processing.core.PApplet;
import processing.core.PImage;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scrabble extends PApplet {
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Screen screen;
    private static Random random;
    private static Dictionary dictionary;
    private static SocketChannel socketChannel;
    private static ConcurrentLinkedQueue<String> queue;
    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    private static Scrabble window;

    public static Scrabble getWindow() {
        return window;
    }


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
            queue = new ConcurrentLinkedQueue<>();
            System.out.println("Connected to the server.");

            // Read events from the console and send them to the server
            startReception();
//            openCmdThread();
            PApplet.main(Scrabble.class.getName(), args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeScreen(Screen screen) {
        Scrabble.screen = screen;
    }

    public static void openCmdThread() { // to be used for development
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a username: ");
            String input = "logon:" + scanner.nextLine();
            sendMessage(input);
            while (true) {
                System.out.print("Enter an event to send (or 'quit' to exit): ");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("quit")) {
                    System.exit(0);
                }

                sendMessage(input);
            }
        }).start();
    }

    public static void startReception() {
        new Thread(() -> {
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
                System.exit(0);
            }
        }).start();
    }

    public static void handleMessage(String res) {
        String[] split = res.split(":");
        if(split.length < 1) {
            System.out.println("Server responded with empty message.");
        }

        // Kind of bad temporary variable stuff but whatever
        String[] rest = Arrays.copyOfRange(split, 1, split.length);

        System.out.println("Received server message, id: " + split[0] + ", data: " + Arrays.toString(rest));
        if(!screen.handleMessage(split[0], rest)) {
            System.out.println("Unsupported message: " + split[0]);
        }
    }

    public static void sendMessage(String msg) {
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
            handleMessage(queue.poll());
        }
    }

    public void createExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
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

        window = this;
        screen = new WelcomeScreen();

        createExitHandler();
        windowTitle("Phrases with Phriends");

        URL iconResource = Objects.requireNonNull(this.getClass().getResource("/icon.png"));
        try {
            Image image = ImageIO.read(iconResource);
            // We probably won't update the library so this doesn't really matter
            @SuppressWarnings("deprecation")
            PImage icon = new PImage(image);
            surface.setIcon(icon);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load app icon.");
        }

        random = new Random();

        // Load the dictionary
        try {
            dictionary = Dictionary.loadFromFile("/dictionary.txt");
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load dictionary. Exiting!");
            System.exit(1);
        }

        System.out.println("Loaded dictionary with " + dictionary.size() + " words");
    }

    @Override
    public void draw() {
        process();
        screen.onFrame(this.g);
    }

    @Override
    public void mousePressed() {
        screen.mousePressed(mouseButton);
    }

    @Override
    public void mouseReleased() {
        screen.mouseReleased(mouseButton);
    }

    @Override
    public void keyPressed() {
        screen.keyPressed(key, keyCode);
    }
}
