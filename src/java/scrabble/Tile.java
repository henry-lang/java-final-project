package scrabble;

import processing.core.PGraphics;

public class Tile {
    private final char letter;
    private final int value;
    private final boolean isBlank;

    // Whether this tile is incorporated onto the board or is temporarily placed down
    private boolean isFinalized = false;

    public Tile(char letter, int value, boolean isBlank) {
        this.letter = Character.toUpperCase(letter);
        this.value = value;
        this.isBlank = isBlank;
    }

    public Tile(char letter, boolean isBlank) {
        this.letter = Character.toUpperCase(letter);
        this.value = 0;
        this.isBlank = isBlank;
    }

    public void makeFinalized() {
        isFinalized = true;
    }

    public int getValue() {
        return value;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public char getLetter() {
        return letter;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    // TODO: FINISH THIS METHOD SO THE CODE IS MORE MANAGEABLE
    public void draw(PGraphics graphics, float x, float y, float size) {

    }
}
