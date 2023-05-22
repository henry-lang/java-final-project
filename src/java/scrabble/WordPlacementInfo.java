package scrabble;

import java.util.ArrayList;

public class WordPlacementInfo {
    public static final WordPlacementInfo INVALID_STRAIGHT_LINE = WordPlacementInfo.invalid("Tiles must be in a straight line");
    public static final WordPlacementInfo INVALID_NO_TILES = WordPlacementInfo.invalid("No tiles placed");
    public static final WordPlacementInfo INVALID_GAP_IN_LINE = WordPlacementInfo.invalid("Gaps between tiles are not allowed");
    public static final WordPlacementInfo INVALID_CENTER_SQUARE = WordPlacementInfo.invalid("First move must use center square");
    public static final WordPlacementInfo INVALID_TOUCH_EXISTING = WordPlacementInfo.invalid("Move must touch existing tiles");
    public final boolean isValid;
    public final String invalidReason;
    public final ArrayList<WordPlacement> words;

    private WordPlacementInfo(boolean isValid, String invalidReason, ArrayList<WordPlacement> words) {
        this.isValid = isValid;
        this.invalidReason = invalidReason;
        this.words = words;
    }

    public static WordPlacementInfo valid(ArrayList<WordPlacement> words) {
        return new WordPlacementInfo(true, null, words);
    }

    public static WordPlacementInfo invalid(String invalidReason) {
        return new WordPlacementInfo(false, invalidReason, null);
    }
}
