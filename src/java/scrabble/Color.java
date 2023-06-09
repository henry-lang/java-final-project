package scrabble;

// This class is here only because I need to store colors easily without a PGraphics instance
public class Color {
    // A constant representing the color that should be used as the background in the menus
    public static final Color MENU_COLOR = new Color(66, 170, 255);

    // Red component of the color, range 0-255
    public int r;

    // Green component of the color, range 0-255
    public int g;

    // Blue component of the color, range 0-255
    public int b;

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color(int b) {
        this(b, b, b);
    }
}
