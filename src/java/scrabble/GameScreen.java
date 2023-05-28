package scrabble;

import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.LEFT;
import static scrabble.Board.TILE_SIZE;

public class GameScreen implements Screen {
    private static final Board board = new Board();
    private static final TileRack rack = new TileRack();

    private static Tile draggedTile = null;


    public GameScreen() {
        WordPlacementInfo placement = board.checkWordPlacement();
        if(placement.isValid) {
            placement.words.forEach(p -> System.out.println(p.pointValue + " " + p.word));
        } else {
            System.out.println(placement.invalidReason);
        }
    }

    @Override
    public Screen onFrame(PGraphics graphics) {
        graphics.background(200);
        board.draw(graphics);
        rack.draw(graphics);
        float x = Scrabble.getWindow().mouseX - TILE_SIZE / 2;
        float y = Scrabble.getWindow().mouseY - TILE_SIZE / 2;
        float textX = x + TILE_SIZE / 2;
        float textY = y + TILE_SIZE * 0.7f;
        if(draggedTile != null) {
            // TODO: do not do this and write a Tile.draw() method.
            graphics.fill(242, 173, 26);
            graphics.rect(x, y, TILE_SIZE, TILE_SIZE, Board.TILE_RADIUS);
            graphics.fill(0);
            graphics.text(draggedTile.getLetter(), textX, textY);
        }

        return null;
    }

    @Override
    public void handleServerMessage() {

    }

    @Override
    public void mousePressed(int mouseButton) {
        if(mouseButton != LEFT) return;
        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;

        Tile startDrag; // The tile that should start being dragged
        if(mouseY < Scrabble.getWindow().width) {
            startDrag = board.tryDrag(mouseX, mouseY);
        } else {
            startDrag = rack.tryDrag(mouseX, mouseY);
        }

        if(startDrag != null) {
            draggedTile = startDrag;
        }
    }

    @Override
    public void mouseReleased(int mouseButton) {
        if(mouseButton != LEFT) return;
        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;

        if(mouseY < Scrabble.getWindow().width) {
            if(!board.tryDrop(mouseX, mouseY, draggedTile)) {
                rack.add(draggedTile);
            }
        } else {
            rack.drop(mouseX, mouseY, draggedTile);
        }
        draggedTile = null;
    }

    @Override
    public void keyPressed() {

    }
}
