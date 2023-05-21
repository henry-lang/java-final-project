package scrabble;

public enum Multiplier {
    NONE(new Color(240)),
    ORIGIN(new Color(130, 48, 83)),
    DL(new Color(20, 119,183)),
    TL(new Color(113, 163, 89)),
    DW(new Color(172, 68, 55)),
    TW(new Color(229, 149, 60));

    private final Color color;

    Multiplier(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
