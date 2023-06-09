package scrabble;

// This enum represents the type of multiplier that a given square on the board has
public enum Multiplier {
    // No multiplier
    NONE(new Color(240)),

    // The origin of the board - this isn't a multiplier but is a special square
    ORIGIN(new Color(130, 48, 83)),

    // Double letter multiplier
    DL(new Color(20, 119,183)),

    // Triple letter multiplier
    TL(new Color(113, 163, 89)),

    // Double word multiplier
    DW(new Color(172, 68, 55)),

    // Triple letter multiplier
    TW(new Color(229, 149, 60));

    // The color that a given multiplier has and will be rendered as
    private final Color color;

    Multiplier(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
