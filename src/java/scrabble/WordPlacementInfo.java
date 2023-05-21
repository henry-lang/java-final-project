package scrabble;

public class WordPlacementInfo {
    public static final WordPlacementInfo INVALID_STRAIGHT_LINE = new WordPlacementInfo(false, 0, "Tiles must be in a straight line");
    public static final WordPlacementInfo INVALID_NO_TILES = new WordPlacementInfo(false, 0, "No tiles placed");
    public static final WordPlacementInfo INVALID_GAP_IN_LINE = new WordPlacementInfo(false, 0, "Gaps between tiles are not allowed");
    public static final WordPlacementInfo INVALID_CENTER_SQUARE = new WordPlacementInfo(false, 0, "First move must use center square");
    public static final WordPlacementInfo INVALID_TOUCH_EXISTING = new WordPlacementInfo(false, 0, "Move must touch existing tiles");
    public final boolean isValid;
    public final int pointValue;
    public final String invalidReason;

    public WordPlacementInfo(boolean isValid, int pointValue, String invalidReason) {
        this.isValid = isValid;
        this.pointValue = pointValue;
        this.invalidReason = invalidReason;
    }
}
