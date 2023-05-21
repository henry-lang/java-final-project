package scrabble;

public class Tile {
    private final int value;
    private final boolean isBlank;
    private final char letter;

    // Whether this tile is incorporated onto the board or is temporarily placed down
    private boolean isPlacedDown = false;

    public Tile(int value, boolean isBlank, char letter) {
        this.value = value;
        this.isBlank = isBlank;
        this.letter = letter;
    }

    public void placeDown() {
        isPlacedDown = true;
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

    public boolean isPlacedDown() {
        return isPlacedDown;
    }
}
