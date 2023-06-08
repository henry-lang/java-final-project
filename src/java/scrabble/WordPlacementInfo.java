package scrabble;

import java.util.ArrayList;

public class WordPlacementInfo {
    public static final WordPlacementInfo INVALID_STRAIGHT_LINE = invalid("Tiles must be in a straight line");
    public static final WordPlacementInfo INVALID_NO_TILES = invalid("No tiles placed");
    public static final WordPlacementInfo INVALID_GAP_IN_LINE = invalid("Gaps between tiles are not allowed");
    public static final WordPlacementInfo INVALID_CENTER_SQUARE = invalid("First move must use center square");
    public static final WordPlacementInfo INVALID_TOUCH_EXISTING = invalid("Move must touch existing tiles");
    public final boolean isValid;
    public final String invalidReason;
    public final ArrayList<WordPlacement> words;
    public final int points;

    private WordPlacementInfo(boolean isValid, String invalidReason, ArrayList<WordPlacement> words) {
        this.isValid = isValid;
        this.invalidReason = invalidReason;
        this.words = words;
        if(isValid) {
            int points = 0;
            for(WordPlacement word: words) {
                points += word.pointValue;
            }
            this.points = points;
        } else {
            points = 0;
        }
    }

    public static WordPlacementInfo valid(ArrayList<WordPlacement> words) {
        return new WordPlacementInfo(true, null, words);
    }

    public static WordPlacementInfo invalid(String invalidReason) {
        return new WordPlacementInfo(false, invalidReason, null);
    }

    public static WordPlacementInfo invalidWord(String word) {
        return invalid("Invalid word: " + word);
    }
}
