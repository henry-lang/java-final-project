package scrabble;

import processing.core.PApplet;

import java.io.IOException;

public class Scrabble extends PApplet {
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;

    private static Dictionary dictionary;
    private static Board board;

    public static Dictionary getDictionary() {
        return dictionary;
    }

    public static void main(String[] args) {
        PApplet.main(Scrabble.class.getName(), args);
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
