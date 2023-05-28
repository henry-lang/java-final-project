package scrabble;

import processing.core.PGraphics;

public class TileRack {
    private final float TILE_SIZE = 45;
    private final float TILE_GAP = 10;

    private final Tile[] tiles = new Tile[7];

    public TileRack() {
        // to be removed later
        for (int i = 0; i < 7; i++) add(new Tile('a', 1, false));
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
            if(tiles[i] == null) continue;
            float x = (TILE_SIZE * i) + 60 + (TILE_GAP * i);
            float xTxt = x + (TILE_SIZE / 2.0f);
            float yTxt = 527.5f + (TILE_SIZE * 0.65f);
            graphics.fill(242, 173, 26);
            graphics.rect(x, 527.5f, TILE_SIZE, TILE_SIZE, 10);
            graphics.fill(0);
            graphics.text(tiles[i].getLetter(), xTxt, yTxt);
        }
    }

    private int getMouseIndex(float mouseX, float mouseY) {
        // TODO: stop hardcoding here lol
        if(mouseY < 527.5f || mouseY > 527.5f + TILE_SIZE) return -1;
        float tileRatio = 1 - (TILE_GAP / (TILE_SIZE + TILE_GAP));
        float tileIndex = ((mouseX - 60) / (TILE_SIZE + TILE_GAP));
        if(tileIndex % 1 >= tileRatio || tileIndex < 0 || tileIndex >= tiles.length) return -1;
        return (int) tileIndex;
    }

    public Tile tryDrag(float mouseX, float mouseY) {
        int index = getMouseIndex(mouseX, mouseY);
        if(index == -1) {
            return null;
        }
        Tile tile = tiles[index];
        tiles[index] = null;
        return tile;
    }

    public void drop(float mouseX, float mouseY, Tile tile) {
        int index = getMouseIndex(mouseX, mouseY);

        if(index == -1 || tiles[index] != null) {
            add(tile);
        } else {
            tiles[index] = tile;
        }
    }
}
