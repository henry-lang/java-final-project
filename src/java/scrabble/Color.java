package scrabble;

// This class is here only because I need to store colors easily without a PGraphics instance
public class Color {
    public static final Color MENU_COLOR = new Color(66, 170, 255);

    public int r;
    public int g;
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
