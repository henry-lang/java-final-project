package scrabble;

import processing.core.PGraphics;

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
