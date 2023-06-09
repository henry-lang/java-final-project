package scrabble;

class TileLineInfo {
    // If the tiles are in a straight line
    public final boolean inStraightLine;

    // If the tile line is horizontal
    public final boolean horizontal;

    // The start row of the tile line
    public final int startRow;

    // The start col of the tile line
    public final int startCol;

    // The end row of the tile line
    public final int endRow;

    // The end col of the tile line
    public final int endCol;

    // A constant representing a tile line info that is invalid because it isn't in a straight line
    public static final TileLineInfo NOT_IN_STRAIGHT_LINE = new TileLineInfo(false, false, -1, -1, -1, -1);

    public TileLineInfo(boolean inStraightLine, boolean horizontal, int startRow, int startCol, int endRow, int endCol) {
        this.inStraightLine = inStraightLine;
        this.horizontal = horizontal;
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    // If the line intersects a board space
    public boolean intersects(int row, int col) {
        return horizontal
                ? (row == 0 && col >= startCol && col <= endCol)
                : (col == 0 && row >= startRow && row <= endRow);
    }
}