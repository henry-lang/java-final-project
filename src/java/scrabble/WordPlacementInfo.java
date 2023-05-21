package scrabble;

public class WordPlacementInfo {
    public static final WordPlacementInfo INVALID_STRAIGHT_LINE = new WordPlacementInfo(false, 0, "Tiles must be in a straight line");
    public static final WordPlacementInfo INVALID_NO_TILES = new WordPlacementInfo(false, 0, "No tiles placed");
    public final boolean isValid;
    public final int pointValue;
    public final String invalidReason;

    public WordPlacementInfo(boolean isValid, int pointValue, String invalidReason) {
        this.isValid = isValid;
        this.pointValue = pointValue;
        this.invalidReason = invalidReason;
    }
}
