package scrabble;

class TileLineInfo {
    public final boolean inStraightLine;
    public final boolean horizontal;
    public final int startRow;
    public final int startCol;
    public final int endRow;
    public final int endCol;

    public static final TileLineInfo NOT_IN_STRAIGHT_LINE = new TileLineInfo(false, false, -1, -1, -1, -1);

    public TileLineInfo(boolean inStraightLine, boolean horizontal, int startRow, int startCol, int endRow, int endCol) {
        this.inStraightLine = inStraightLine;
        this.horizontal = horizontal;
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public boolean intersects(int row, int col) {
        return horizontal
                ? (row == 0 && col >= startCol && col <= endCol)
                : (col == 0 && row >= startRow && row <= endRow);
    }
}