package scrabble;

import processing.core.PGraphics;

import java.util.Arrays;

import static processing.core.PConstants.LEFT;
import static scrabble.Board.TILE_SIZE;

public class GameScreen implements Screen {
    private final Board board = new Board();

    // Whether it's this client's turn.
    private boolean thisTurn;
    private int thisScore = 0;
    private int opponentScore = 0;

    private final TileRack rack;

    private static Tile draggedTile = null;


    public GameScreen(String opponent, Tile[] tiles, boolean thisTurn) {
        rack = new TileRack(tiles);
        this.thisTurn = thisTurn;
        System.out.println(thisTurn);
    }

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float boardEnd = Board.Y + Board.SIZE * (Board.TILE_SIZE + Board.TILE_GAPS);
        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        board.draw(graphics);
        rack.draw(graphics);
        graphics.fill(255);
        if(board.checkWordPlacement().isValid) {
            if(thisTurn) {
                graphics.rect(screenCenter - 80, boardEnd + 5, 160, 35, 25);
                graphics.fill(0);
                graphics.text("PLAY - ", screenCenter, boardEnd + 40 * 0.7f);
            } else {
                // TODO: say how many points it WOULD be worth
            }
        } else {
            graphics.text(board.checkWordPlacement().invalidReason, screenCenter, boardEnd + 30);
        }
        float x = Scrabble.getWindow().mouseX - TILE_SIZE / 2;
        float y = Scrabble.getWindow().mouseY - TILE_SIZE / 2;
        float textX = x + TILE_SIZE / 2;
        float textY = y + TILE_SIZE * 0.7f;
        if(draggedTile != null) {
            // TODO: do not do this and write a Tile.draw() method.
            graphics.noStroke();
            graphics.fill(242, 173, 26);
            graphics.rect(x, y, TILE_SIZE, TILE_SIZE, Board.TILE_RADIUS);
            graphics.fill(0);
            graphics.text(draggedTile.getLetter(), textX, textY);
        }
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            case "turn_success": {
                thisScore += board.checkWordPlacement().points;
                board.finalizeTurn();
                Tile[] newTiles = Parsing.parseTiles(data[0]);
                for(Tile tile : newTiles) {
                    rack.add(tile);
                }
                return true;
            }

            case "turn_fail": {
                // TODO: i don't think there's too much we can do here to notify the user, i mean it's not their fault
                System.out.println("Failed submitting seemingly valid turn: " + data[0]);
                return true;
            }

            case "opponent_turn": {
                board.applyOpponentTurn(data);
                return true;
            }

            default: {
                return false;
            }
        }
    }

    @Override
    public void mousePressed(int mouseButton) {
        if(mouseButton != LEFT) return;
        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;

        Tile startDrag; // The tile that should start being dragged
        if(mouseY < Scrabble.getWindow().width + Board.Y && mouseY > Board.Y) {
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
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float boardEnd = Board.Y + Board.SIZE * (Board.TILE_SIZE + Board.TILE_GAPS);

        if(draggedTile != null) {
            if(mouseY < Scrabble.getWindow().width + Board.Y && mouseY > Board.Y) {
                if(!board.tryDrop(mouseX, mouseY, draggedTile)) {
                    rack.add(draggedTile);
                }
            } else {
                rack.drop(mouseX, mouseY, draggedTile);
            }

            draggedTile = null;
        } else if(mouseX > screenCenter - 80 && mouseX < screenCenter + 80 && mouseY > boardEnd + 5 && mouseY < boardEnd + 40) {
            Scrabble.sendMessage(board.getTurnMessage());
        }
    }
}