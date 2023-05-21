package scrabble;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static scrabble.Multiplier.*;

public class Board {
    public static final int SIZE = 15;
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

        tiles[1][1] = new Tile('c', 3, false);
        tiles[2][1] = new Tile('a', 1, false);
        tiles[3][1] = new Tile('t', 2, false);
    }

    private boolean anyTilesPlaced() {
        var isPlaced = false;
        outer:
        for (var row : tiles) {
            for (var tile : row) {
                if (tile != null) {
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
    }

    private TileLineInfo tilesInStraightLine() {
        var startRow = -1;
        var startCol = -1;
        var lastRow = -1;
        var lastCol = -1;
        var decidedDirection = false;
        var horizontal = false; // Only useful when decidedDirection is true
        for(var r = 0; r < SIZE; r++) {
            for(var c = 0; c < SIZE; c++) {
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
        for(var r = lineInfo.startRow; r <= lineInfo.endRow; r++) {
            for(var c = lineInfo.startCol; c <= lineInfo.endCol; c++) {
                if(tiles[r][c] == null) {
                    // There is a gap!
                    return true;
                }
            }
        }
        return false;
    }

    public List<WordPlacementInfo> checkWordPlacement() {
        var wordPlacements = new ArrayList<WordPlacementInfo>();

        if(!anyTilesPlaced()) {
            wordPlacements.add(WordPlacementInfo.INVALID_NO_TILES);
            return wordPlacements;
        }

        var lineInfo = tilesInStraightLine();
        if(!lineInfo.inStraightLine) {
            wordPlacements.add(WordPlacementInfo.INVALID_STRAIGHT_LINE);
            return wordPlacements;
        }

        if(checkForGapsInLine(lineInfo)) {
            wordPlacements.add(WordPlacementInfo.INVALID_GAP_IN_LINE);
            return wordPlacements;
        }

        if(lineInfo.horizontal) {
            var col = lineInfo.startCol;
            while(col > 0 && tiles[lineInfo.startRow][col] != null) {
                col -= 1;
            }
        } else {
            var col = lineInfo.startCol;

            // Extend the line if there are tiles after or before the line
            var startRow = lineInfo.startRow;
            while(startRow > 0 && tiles[startRow - 1][col] != null) {
                startRow -= 1;
            }

            var endRow = lineInfo.endRow;
            while(endRow < SIZE - 1 && tiles[endRow + 1][col] != null) {
                endRow += 1;
            }

            var pointValue = 0;
            var wordMultiplier = 1;
            var word = new StringBuilder();
            for(var row = startRow; row <= endRow; row++) {
                var tile = tiles[row][col];
                var tileMultiplier = 1;
                if(!tile.isFinalized()) {
                    var multiplier = multipliers[row][col];
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
                word.append(tiles[row][col].getLetter());
            }

            pointValue *= wordMultiplier;
            wordPlacements.add(new WordPlacementInfo(true, pointValue, word.toString()));
        }

        System.out.println(lineInfo.horizontal + " " + lineInfo.startRow + " " + lineInfo.startCol + " " + lineInfo.endRow + " " + lineInfo.endCol);

        wordPlacements.sort(Comparator.comparingInt(a -> a.pointValue));
        return wordPlacements;
    }

    public void draw(PGraphics graphics) {
        graphics.noStroke();
        graphics.fill(200);
        graphics.rect(0, 0, graphics.width, graphics.width);

        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                var tile = tiles[i][j];
                var multiplier = multipliers[i][j];
                var color = multiplier.getColor();

                if(tile == null) {
                    graphics.fill(color.r, color.g, color.b);
                    graphics.rect(i * BOARD_SPACING + TILE_GAPS / 2, j * BOARD_SPACING + TILE_GAPS / 2, TILE_SIZE, TILE_SIZE, TILE_RADIUS);
                    if(multiplier != NONE && multiplier != ORIGIN) {
                        graphics.textSize(TILE_SIZE * 0.7f);
                        graphics.textAlign(graphics.CENTER);
                        graphics.fill(240);
                        graphics.text(multiplier.name(), i * BOARD_SPACING + TILE_GAPS / 2 + TILE_SIZE / 2, j * BOARD_SPACING + TILE_GAPS / 2 + TILE_SIZE * 0.7f);
                    }
                } else {
                    // Get status of neighboring cells
                    var up = i > 0 && tiles[i - 1][j] != null;
                    var down = i < SIZE - 1 && tiles[i + 1][j] != null;
                    var left = j > 0 && tiles[i][j - 1] != null;
                    var right = j < SIZE - 1 && tiles[i][j + 1] != null;

                    var tl = !up && !left ? TILE_RADIUS : 0;
                    var tr = !up && !right ? TILE_RADIUS : 0;
                    var bl = !down && !left ? TILE_RADIUS : 0;
                    var br = !down && !right ? TILE_RADIUS : 0;

                    var x = j * BOARD_SPACING + TILE_GAPS / 2;
                    var y = i * BOARD_SPACING + TILE_GAPS / 2;
                    var w = TILE_SIZE;
                    var h = TILE_SIZE;

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

                    graphics.fill(255, 255, 0);
                    graphics.rect(x, y, w, h, tl, tr, br, bl);
                }
            }
        }
    }
}
