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
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scrabble extends PApplet {
    // The width of the window in pixels
    public static final int WINDOW_WIDTH = 500;

    // The height of the window in pixels
    public static final int WINDOW_HEIGHT = 665;

    // The host ip of the server
    private static final String SERVER_HOST = "localhost";

    // The port to connect to the server on
    private static final int SERVER_PORT = 8080;

    // The current screen state of the application
    private static Screen screen;

    // The random number generator
    private static Random random;

    // The currently loaded dictionary
    private static Dictionary dictionary;

    // The socket connection to the server
    private static SocketChannel socketChannel;

    // The queue of messages that are yet to be processed by the current screen - it's a ConcurrentLinkedQueue because
    // this is used between two threads of execution and therefore we don't want race conditions
    private static ConcurrentLinkedQueue<String> queue;

    // The butter used for storing the length of the message - basically allows us to reuse memory instead of allocating
    // every time
    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    // The actual object of this class that allows us to access things like mouseX from Screen classes
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

    // Start reading events from the server on another thread, putting them in the message queue when they are ready to
    // be processed
    public static void startReception() {
        new Thread(() -> {
            try {
                while (true) {
                    lengthBuffer.clear();
                    int bytesRead = socketChannel.read(lengthBuffer);
                    if (bytesRead == -1) break; // Connection closed by server

                    if (lengthBuffer.position() == Integer.BYTES) { // copied from server
                        lengthBuffer.flip();
                        // Retrieve the length of the message
                        int messageSize = lengthBuffer.getInt();
                        ByteBuffer buffer = ByteBuffer.allocate(messageSize);
                        socketChannel.read(buffer);
                        String msg = new String(buffer.array());

                        // Add it to the queue
                        queue.offer(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }).start();
    }

    // Handle a message from the server in type:data0:data1:data2... format and send it to the current screen
    public static void handleMessage(String res) {
        String[] split = res.split(":");
        // If there is not even a type (first part of the message) then something's gone a bit wrong
        if(split.length < 1) {
            System.out.println("Server responded with empty message.");
        }

        // Kind of bad temporary variable stuff but whatever
        // Copy the rest of the message which is from index 1 (after the type) all the way to the end
        String[] rest = Arrays.copyOfRange(split, 1, split.length);

        System.out.println("Received server message, id: " + split[0] + ", data: " + Arrays.toString(rest));
        // Send the message to the current screen
        if(!screen.handleMessage(split[0], rest)) {
            // If the current screen couldn't handle this message, log it because something might be wrong
            System.out.println("Unsupported message: " + split[0]);
        }
    }

    // Send a message to the server
    public static void sendMessage(String msg) {
        try {
            byte[] bytes = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            // Actually write it to the channel
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Process all the current messages in the queue
    public void process() {
        while (!queue.isEmpty()) {
            // Send the message to the current screen
            handleMessage(queue.poll());
        }
    }

    // This code just adds a hook to make sure the server connection is closed when the Java Runtime exits
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

    // This method sets initial settings for the window
    @Override
    public void settings() {
        // Set the size of the window
        size(WINDOW_WIDTH, WINDOW_HEIGHT);

        // If we are on a Mac or other display with a High DPI (dots per inch), use Processing's ability to make it look
        // sharp.
        int displayDensity = displayDensity();
        pixelDensity(displayDensity);
        System.out.println("Display Density: " + displayDensity);
    }

    // This method runs once after setup
    @Override
    public void setup() {
        window = this;
        // Set the initial screen to be a welcome screen
        screen = new WelcomeScreen();

        createExitHandler();
        windowTitle("Phrases with Phriends");

        // Load the desktop application image from /icon.png and set it as the icon of the app.
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

    // This is run every frame (60th of a second)
    @Override
    public void draw() {
        // Process any events
        process();
        // Call the screen's onFrame function and pass in the graphics context for rendering
        screen.onFrame(this.g);
    }

    // ALL BELOW METHODS SIMPLY DISPATCH USER EVENTS TO THE CURRENT SCREEN

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
