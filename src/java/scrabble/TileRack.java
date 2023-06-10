package scrabble;

import processing.core.PGraphics;

public class TileRack {
    // The size in pixels of each tile in the rack
    private static final float TILE_SIZE = 45;

    // The gap in pixels between tiles on the rack
    private static final float TILE_GAP = 10;

    // The Y coordinate start of the rack
    public static final float Y = Scrabble.WINDOW_HEIGHT - TILE_SIZE - TILE_GAP;

    // The actual list of tiles
    private final Tile[] tiles;

    public TileRack(Tile[] tiles) {
        this.tiles = tiles;
    }

    // Add a new tile to the rack in the first available space
    public void add(Tile newTile) {
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] != null) continue;
            tiles[i] = newTile;
            break;
        }
    }

    public int numTiles() {
        int num = 0;
        for(Tile tile : tiles) {
            if(tile != null) {
                num++;
            }
        }

        return num;
    }

    // Remove a tile at a given index
    public void remove(int index) {
        tiles[index] = null;
    }

    // Shuffle the tile rack
    public void shuffle() {
        for(int i = 0; i < tiles.length; i++) {
            int otherIndex = Scrabble.getRandom().nextInt(tiles.length);
            Tile temp = tiles[i];
            tiles[i] = tiles[otherIndex];
            tiles[otherIndex] = temp;
        }
    }

    // Draw the tile rack
    public void draw(PGraphics graphics) {
        graphics.fill(230);
        graphics.rect(50f, Y, 400f, 50f, 15);
        // TODO: refactor this
        for (int i = 0; i < tiles.length; i++) {
            if(tiles[i] == null) continue;
            float x = (TILE_SIZE * i) + 60 + (TILE_GAP * i);
            float xTxt = x + (TILE_SIZE / 2.0f);
            float yTxt = Y + 2.5f + (TILE_SIZE * 0.75f);
            graphics.fill(242, 173, 26);
            graphics.rect(x, Y + 2.5f, TILE_SIZE, TILE_SIZE, 10);
            graphics.fill(0);
            graphics.textSize(TILE_SIZE * 0.7f);
            graphics.text(tiles[i].getLetter(), xTxt, yTxt);
            graphics.textSize(TILE_SIZE * 0.4f);
            graphics.text(tiles[i].getValue(), xTxt + TILE_SIZE * 0.30f, yTxt + TILE_SIZE * 0.20f);
        }
    }

    // Get the index of the tile rack that the mouse is currently over, or -1 if it isn't on a tile
    private int getMouseIndex(float mouseX, float mouseY) {
        // TODO: stop hardcoding here lol
        if(mouseY < Y + 2.5f || mouseY > Y + 2.5f + TILE_SIZE) return -1;
        float tileRatio = 1 - (TILE_GAP / (TILE_SIZE + TILE_GAP));
        float tileIndex = ((mouseX - 60) / (TILE_SIZE + TILE_GAP));
        if(tileIndex % 1 >= tileRatio || tileIndex < 0 || tileIndex >= tiles.length) return -1;
        return (int) tileIndex;
    }

    // Attempts to return the tile that the mouse is on, if possible
    public Tile tryDrag(float mouseX, float mouseY) {
        int index = getMouseIndex(mouseX, mouseY);
        if(index == -1) {
            return null;
        }
        Tile tile = tiles[index];
        tiles[index] = null;
        return tile;
    }

    // Drops the tile onto the rack onto the mouse index if possible and the first available space otherwise
    public void drop(float mouseX, float mouseY, Tile tile) {
        int index = getMouseIndex(mouseX, mouseY);

        if(index == -1 || tiles[index] != null) {
            add(tile);
        } else {
            tiles[index] = tile;
        }
    }
}
