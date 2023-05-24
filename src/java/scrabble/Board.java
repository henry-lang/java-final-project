package scrabble;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import static scrabble.Multiplier.*;

public class Board {
    public static final int SIZE = 15;
    public static final int CENTER = SIZE / 2;
    public static final float BOARD_SPACING = ((float) Scrabble.WINDOW_WIDTH) / SIZE;
    public static final float TILE_SIZE = BOARD_SPACING * 0.85f;
    public static final float TILE_GAPS = BOARD_SPACING - TILE_SIZE;
    public static final float TILE_RADIUS = TILE_SIZE * 0.15f;

    public static final Multiplier[][] multipliers = {
            {NONE, NONE, NONE, TW, NONE, NONE, TL, NONE, TL, NONE, NONE, TW, NONE, NONE, NONE},
            {NONE, NONE, DL, NONE, NONE, DW, NONE, NONE, NONE, DW, NONE, NONE, DL, NONE, NONE},
            {NONE, DL, NONE, NONE, DL, NONE, NONE, NONE, NONE, NONE, DL, NONE, NONE, DL, NONE},
            {TW, NONE, NONE, TL, NONE, NONE, NONE, DW, NONE, NONE, NONE, TL, NONE, NONE, TW},
            {NONE, NONE, DL, NONE, NONE, NONE, DL, NONE, DL, NONE, NONE, NONE, DL, NONE, NONE},
            {NONE, DW, NONE, NONE, NONE, TL, NONE, NONE, NONE, TL, NONE, NONE, NONE, DW, NONE},
            {TL, NONE, NONE, NONE, DL, NONE, NONE, NONE, NONE, NONE, DL, NONE, NONE, NONE, TL},
            {NONE, NONE, NONE, DW, NONE, NONE, NONE, ORIGIN, NONE, NONE, NONE, DW, NONE, NONE, NONE},
            {TL, NONE, NONE, NONE, DL, NONE, NONE, NONE, NONE, NONE, DL, NONE, NONE, NONE, TL},
            {NONE, DW, NONE, NONE, NONE, TL, NONE, NONE, NONE, TL, NONE, NONE, NONE, DW, NONE},
            {NONE, NONE, DL, NONE, NONE, NONE, DL, NONE, DL, NONE, NONE, NONE, DL, NONE, NONE},
            {TW, NONE, NONE, TL, NONE, NONE, NONE, DW, NONE, NONE, NONE, TL, NONE, NONE, TW},
            {NONE, DL, NONE, NONE, DL, NONE, NONE, NONE, NONE, NONE, DL, NONE, NONE, DL, NONE},
            {NONE, NONE, DL, NONE, NONE, DW, NONE, NONE, NONE, DW, NONE, NONE, DL, NONE, NONE},
            {NONE, NONE, NONE, TW, NONE, NONE, TL, NONE, TL, NONE, NONE, TW, NONE, NONE, NONE},
    };

    // For now, null in the array indicates that the tile is empty
    // Unfortunately, java doesn't have good enum types like Rust where Option<Tile> is possible
    private final Tile[][] tiles = new Tile[SIZE][SIZE];

    {
//        tiles[1][1] = new Tile('c', 3, false);
//        tiles[1][2] = new Tile('a', 1, false);
//        tiles[1][4] = new Tile('t', 2, false);

        tiles[CENTER - 1][CENTER] = new Tile('c', 3, false);
        tiles[CENTER - 1][CENTER].makeFinalized();
        tiles[CENTER][CENTER] = new Tile('a', 1, false);
        tiles[CENTER][CENTER].makeFinalized();
        tiles[CENTER + 1][CENTER] = new Tile('t', 2, false);
        tiles[CENTER + 1][CENTER].makeFinalized();
    }

    private boolean isFirstMove() {
        return tiles[CENTER][CENTER] == null;
    }

    private boolean anyTilesPlaced() {
        boolean isPlaced = false;
        outer:
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile != null && !tile.isFinalized()) {
                    isPlaced = true;
                    break outer;
                }
            }
        }

        return isPlaced;
    }

    private static class TileLineInfo {
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

    private TileLineInfo tilesInStraightLine() {
        int startRow = -1;
        int startCol = -1;
        int lastRow = -1;
        int lastCol = -1;
        boolean decidedDirection = false;
        boolean horizontal = false; // Only useful when decidedDirection is true
        for(int r = 0; r < SIZE; r++) {
            for(int c = 0; c < SIZE; c++) {
                if(tiles[r][c] != null && !tiles[r][c].isFinalized()) {
                    if(lastRow != -1 && lastCol != -1) {
                        if(decidedDirection) {
                            if((horizontal && r != lastRow) || (!horizontal && c != lastCol)) {
                                return TileLineInfo.NOT_IN_STRAIGHT_LINE;
                            }
                        } else {
                            if(lastRow == r) {
                                horizontal = true;
                            } else if(lastCol == c) {
                                horizontal = false;
                            } else {
                                return TileLineInfo.NOT_IN_STRAIGHT_LINE;
                            }
                        }
                        decidedDirection = true;
                    }

                    if(startRow == -1 && startCol == -1) {
                        startRow = r;
                        startCol = c;
                    }

                    lastRow = r;
                    lastCol = c;
                }
            }
        }

        return new TileLineInfo(true, horizontal, startRow, startCol, lastRow, lastCol);
    }

    private boolean checkForGapsInLine(TileLineInfo lineInfo) {
        for(int r = lineInfo.startRow; r <= lineInfo.endRow; r++) {
            for(int c = lineInfo.startCol; c <= lineInfo.endCol; c++) {
                if(tiles[r][c] == null) {
                    // There is a gap!
                    return true;
                }
            }
        }
        return false;
    }

    public WordPlacementInfo checkWordPlacement() {

        if(!anyTilesPlaced()) {
            return WordPlacementInfo.INVALID_NO_TILES;
        }

        TileLineInfo lineInfo = tilesInStraightLine();
        if(!lineInfo.inStraightLine) {
            return WordPlacementInfo.INVALID_STRAIGHT_LINE;
        }

        if(checkForGapsInLine(lineInfo)) {
            return WordPlacementInfo.INVALID_GAP_IN_LINE;
        }

        if(isFirstMove() && !lineInfo.intersects(CENTER, CENTER)) {
            return WordPlacementInfo.INVALID_CENTER_SQUARE;
        }

        ArrayList<WordPlacement> words = new ArrayList<>();
        if(lineInfo.horizontal) {
            int col = lineInfo.startCol;
            while(col > 0 && tiles[lineInfo.startRow][col] != null) {
                col -= 1;
            }
        } else {
            int col = lineInfo.startCol;

            // Extend the line if there are tiles after or before the line
            int startRow = lineInfo.startRow;
            while(startRow > 0 && tiles[startRow - 1][col] != null) {
                startRow -= 1;
            }

            int endRow = lineInfo.endRow;
            while(endRow < SIZE - 1 && tiles[endRow + 1][col] != null) {
                endRow += 1;
            }

            int pointValue = 0;
            int wordMultiplier = 1;
            StringBuilder wordBuilder = new StringBuilder();
            for(int row = startRow; row <= endRow; row++) {
                Tile tile = tiles[row][col];
                int tileMultiplier = 1;
                if(!tile.isFinalized()) {
                    Multiplier multiplier = multipliers[row][col];
                    switch(multiplier) {
                        case DL: {
                            tileMultiplier = 2;
                            break;
                        }
                        case TL: {
                            tileMultiplier = 3;
                            break;
                        }
                        case DW: {
                            wordMultiplier *= 2;
                            break;
                        }
                        case TW: {
                            wordMultiplier *= 3;
                        }
                    }
                }
                pointValue += tile.getValue() * tileMultiplier;
                wordBuilder.append(tiles[row][col].getLetter());
            }

            pointValue *= wordMultiplier;

            String word = wordBuilder.toString();
            if(!Scrabble.getDictionary().contains(word)) {
                return WordPlacementInfo.invalidWord(word);
            }

            words.add(new WordPlacement(word, pointValue));
        }

        System.out.println(lineInfo.horizontal + " " + lineInfo.startRow + " " + lineInfo.startCol + " " + lineInfo.endRow + " " + lineInfo.endCol);

        words.sort(Comparator.comparingInt(word -> word.pointValue));
        return WordPlacementInfo.valid(words);
    }

    public void draw(PGraphics graphics) {
        graphics.noStroke();
        graphics.fill(200);
        graphics.rect(0, 0, graphics.width, graphics.width);

        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                Tile tile = tiles[i][j];
                Multiplier multiplier = multipliers[i][j];
                Color color = multiplier.getColor();

                float x = j * BOARD_SPACING + TILE_GAPS / 2;
                float y = i * BOARD_SPACING + TILE_GAPS / 2;
                float textX = x + TILE_SIZE / 2;
                float textY = y + TILE_SIZE * 0.7f;

                graphics.textSize(TILE_SIZE * 0.7f);
                graphics.textAlign(graphics.CENTER);

                if(tile == null) {
                    graphics.fill(color.r, color.g, color.b);
                    graphics.rect(x, y, TILE_SIZE, TILE_SIZE, TILE_RADIUS);
                    if(multiplier != NONE && multiplier != ORIGIN) {
                        graphics.fill(240);
                        graphics.text(multiplier.name(), textX, textY);
                    }
                } else {
                    // Get status of neighboring cells
                    boolean up = i > 0 && tiles[i - 1][j] != null && tiles[i - 1][j].isFinalized();
                    boolean down = i < SIZE - 1 && tiles[i + 1][j] != null && tiles[i + 1][j].isFinalized();
                    boolean left = j > 0 && tiles[i][j - 1] != null && tiles[i][j - 1].isFinalized();
                    boolean right = j < SIZE - 1 && tiles[i][j + 1] != null && tiles[i][j + 1].isFinalized();

                    float tl = !up && !left ? TILE_RADIUS : 0;
                    float tr = !up && !right ? TILE_RADIUS : 0;
                    float bl = !down && !left ? TILE_RADIUS : 0;
                    float br = !down && !right ? TILE_RADIUS : 0;

                    float w = TILE_SIZE;
                    float h = TILE_SIZE;

                    if(left) {
                        x -= TILE_GAPS;
                        w += TILE_GAPS;
                    }

                    if(right) {
                        w += TILE_GAPS;
                    }

                    if(up) {
                        y -= TILE_GAPS;
                        h += TILE_GAPS;
                    }

                    if(down) {
                        h += TILE_GAPS;
                    }

                    // TODO: don't hardcode this
                    graphics.fill(242, 173, 26);
                    graphics.rect(x, y, w, h, tl, tr, br, bl);
                    graphics.fill(0);
                    graphics.text(tile.getLetter(), textX, textY);
                }
            }
        }
    }
}
