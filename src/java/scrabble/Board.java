package scrabble;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import static scrabble.Multiplier.*;

public class Board {

    public static final float Y = 65f;
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
    private boolean boardChanged = true;
    private WordPlacementInfo wordPlacement = null;

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

    private int getMouseColumn(float mouseX) {
        float tileRatio = 1 - (TILE_GAPS / (TILE_SIZE + TILE_GAPS));
        float column = ((mouseX) / (TILE_SIZE + TILE_GAPS));
        if(column % 1 >= tileRatio || column < 0 || column >= tiles.length) return -1;
        return (int) column;
    }

    private int getMouseRow(float mouseY) {
        float tileRatio = 1 - (TILE_GAPS / (TILE_SIZE + TILE_GAPS));
        float row = ((mouseY - Y) / (TILE_SIZE + TILE_GAPS));
        if(row % 1 >= tileRatio || row < 0 || row >= tiles.length) return -1;
        return (int) row;
    }

    public Tile tryDrag(float mouseX, float mouseY) {
        int col = getMouseColumn(mouseX);
        int row = getMouseRow(mouseY);

        if(col != -1 && row != -1 && tiles[row][col] != null && !tiles[row][col].isFinalized()) {
            Tile tile = tiles[row][col];
            tiles[row][col] = null;
            boardChanged = true;
            return tile;
        } else {
            return null;
        }
    }

    public boolean tryDrop(float mouseX, float mouseY, Tile tile) {
        int col = getMouseColumn(mouseX);
        int row = getMouseRow(mouseY);

        if(col != -1 && row != -1 && tiles[row][col] == null) {
            tiles[row][col] = tile;
            boardChanged = true;
            return true;
        } else {
            return false;
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

    private int[] extendWord(boolean horizontal, int start, int end, int rc) {
        // note: rc means row/column. it's just the other value needed to access the 2d array
        if (horizontal) {
            while (start > 0 && tiles[start - 1][rc] != null) start -= 1;
            while (end < SIZE - 1 && tiles[end + 1][rc] != null) end += 1;
        } else {
            while (start > 0 && tiles[rc][start - 1] != null) start -= 1;
            while (end < SIZE - 1 && tiles[rc][end + 1] != null) end += 1;
        }

        return new int[]{start, end};
    }

    int[] getMultipliers(Tile tile, Multiplier multiplier) {
        int[] multipliers = new int[]{1, 1};
        if (tile.isFinalized()) return multipliers;
        switch (multiplier) {
            case DL: { multipliers[0] = 2; break; }
            case TL: { multipliers[0] = 3; break; }
            case DW: { multipliers[1] = 2; break; }
            case TW: { multipliers[1] = 3; break; }
        }
        return multipliers;
    }

    public WordPlacementInfo validateSubWord(boolean horizontal, int start, int end, int rc) {
        StringBuilder builder = new StringBuilder();
        Tile tile = tiles[0][0];
        int wordMultiplier = 1;
        int pts = 0;
        if (horizontal) {
            for (int c = start; c <= end; c++) {
                int[] multis = getMultipliers(tile, multipliers[rc][c]);
                wordMultiplier *= multis[1];
                pts += tile.getValue() * multis[0];
                builder.append(tile.getLetter());
            }
        } else {
            for (int r = start; r <= end; r++) {
                int[] multis = getMultipliers(tile, multipliers[r][rc]);
                wordMultiplier *= multis[1];
                pts += tile.getValue() * multis[0];
                builder.append(tile.getLetter());
            }
        }

        pts *= wordMultiplier;

        String word = builder.toString();
        if(!Scrabble.getDictionary().contains(word)) {
            return WordPlacementInfo.invalidWord(word);
        }

        ArrayList<WordPlacement> words = new ArrayList<>();
        words.add(new WordPlacement(word, pts));
        return WordPlacementInfo.valid(words);
    }
    public WordPlacementInfo checkWordPlacement() {
        if(!boardChanged) return wordPlacement;

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
        int col = lineInfo.startCol;
        if(lineInfo.horizontal) {
            while(col > 0 && tiles[lineInfo.startRow][col] != null) {
                col -= 1;
            }
        } else {
            // Extend the line if there are tiles after or before the line
            int[] extendedWord = extendWord(false, lineInfo.startRow, lineInfo.endRow, col);
            int startRow = extendedWord[0];
            int endRow = extendedWord[1];

            int pointValue = 0;
            int wordMultiplier = 1;
            StringBuilder wordBuilder = new StringBuilder();
            for (int row = startRow; row <= endRow; row++) {
                extendedWord = extendWord(true, lineInfo.startCol, lineInfo.endCol, row);
//                WordPlacementInfo wpInfo = validateSubWord(false, extendedWord[0], extendedWord[1], row);
//                if (!wpInfo.isValid) return wpInfo;
//                else pointValue += wpInfo.words.get(0).pointValue; // prolly not the behavior we want but oh well

                Tile tile = tiles[row][col];
                int[] multis = getMultipliers(tile, multipliers[row][col]);
                wordMultiplier *= multis[1];
                pointValue += tile.getValue() * multis[0];
                wordBuilder.append(tile.getLetter());
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
        wordPlacement = WordPlacementInfo.valid(words);
        boardChanged = false;

        return wordPlacement;
    }

    public String getTurnMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append("turn:");
        msg.append(wordPlacement.points);
        for(int r = 0; r < SIZE; r++) {
            for(int c = 0; c < SIZE; c++) {
                if(tiles[r][c] != null && !tiles[r][c].isFinalized()) {
                    msg.append(':');
                    msg.append(tiles[r][c].getLetter());
                    msg.append(',');
                    msg.append(r);
                    msg.append(',');
                    msg.append(c);
                }
            }
        }
        System.out.println(msg);
        return msg.toString();
    }

    public void applyOpponentTurn(String[] data) {
        for(int i = 1; i < data.length; i++) {
            String[] attrs = data[i].split(",");
            char c = attrs[0].charAt(0);
            int row = Integer.parseInt(attrs[1]);
            int col = Integer.parseInt(attrs[2]);
            tiles[row][col] = new Tile(c);
            tiles[row][col].makeFinalized();
        }
    }

    public void finalizeTurn() {
        for(int r = 0; r < SIZE; r++) {
            for(int c = 0; c < SIZE; c++) {
                if(tiles[r][c] != null && !tiles[r][c].isFinalized()) {
                    tiles[r][c].makeFinalized();
                }
            }
        }
    }

    public void draw(PGraphics graphics) {
        graphics.noStroke();
        graphics.fill(200);
        graphics.rect(0, Y, graphics.width, graphics.width);

        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                Tile tile = tiles[i][j];
                Multiplier multiplier = multipliers[i][j];
                Color color = multiplier.getColor();

                float x = j * BOARD_SPACING + TILE_GAPS / 2;
                float y = i * BOARD_SPACING + TILE_GAPS / 2 + Y;
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
