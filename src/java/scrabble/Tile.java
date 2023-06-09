package scrabble;

// This class represents a tile either being dragged, in the board, or in the tile rack
public class Tile {
    private static final int[] POINT_VALUES = {
            1, // A
            3, // B
            3, // C
            2, // D
            1, // E
            4, // F
            2, // G
            4, // H
            1, // I
            8, // J
            5, // K
            1, // L
            3, // M
            1, // N
            1, // O
            3, // P
            10, // Q
            1, // R
            1, // S
            1, // T
            1, // U
            4, // V
            4, // W
            8, // X
            4, // Y
            10 // Z
    };

    // The letter on this tile
    private final char letter;

    // Whether this tile is incorporated onto the board or is temporarily placed down
    private boolean isFinalized = false;

    public Tile(char letter) {
        this.letter = Character.toUpperCase(letter);
    }

    public void makeFinalized() {
        isFinalized = true;
    }

    // Get the point value of this tile's letter
    public int getValue() {
        return POINT_VALUES[letter - 'A'];
    }

    public char getLetter() {
        return letter;
    }

    public boolean isFinalized() {
        return isFinalized;
    }
}
