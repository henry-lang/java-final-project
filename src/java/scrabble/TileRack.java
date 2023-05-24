package scrabble;

import processing.core.PGraphics;

public class TileRack {
    private final int TILE_WIDTH = 45;
    private final int TILE_GAP = 10;

    private Tile[] tiles = new Tile[7];
    private int indexDragging = -1;

    public TileRack() {
        // to be removed later
        for (int i = 0; i < 7; i++) add(new Tile('a', 1, false));
    }

    public void handleClick() {

    }

    public void handleRelease() {

    }


    public void add(Tile newTile) {
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] != null) continue;
            tiles[i] = newTile;
            break;
        }
    }

    public void remove(int index) {
        tiles[index] = null;
    }

    public void shuffle() {
        for(int i = 0; i < tiles.length; i++) {
            int otherIndex = Scrabble.getRandom().nextInt(tiles.length);
            Tile temp = tiles[i];
            tiles[i] = tiles[otherIndex];
            tiles[otherIndex] = temp;
        }
    }

    public void draw(PGraphics graphics) {
        graphics.fill(230);
        graphics.rect(50f, 525f, 400f, 50f, 15);
        // TODO: refactor this
        for (int i = 0; i < tiles.length; i++) {
            int x = (TILE_WIDTH * i) + 60 + (TILE_GAP * i);
            float xTxt = (float) ((float) x + ((float) TILE_WIDTH / 2.));
            float yTxt = 527.5f + (TILE_WIDTH * 0.65f);
            graphics.fill(242, 173, 26);
            graphics.rect(x, 527.5f, TILE_WIDTH, TILE_WIDTH, 10);
            graphics.fill(0);
            graphics.text(tiles[i].getLetter(), xTxt, yTxt);
        }
    }
}
