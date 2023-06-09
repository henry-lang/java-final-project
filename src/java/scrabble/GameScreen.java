package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.*;
import static scrabble.Board.TILE_SIZE;

// This screen represents the screen that will be shown when the player is in a game - the board, the tile rack, etc.
public class GameScreen implements Screen {
    // The padding on the left and the right of the scores
    private static final float SCORES_PADDING = 5;

    // The board
    private final Board board = new Board();

    // Whether it's this client's turn
    private boolean thisTurn;

    // This player's score
    private int thisScore = 0;

    // The opponent's score
    private int opponentScore = 0;

    // This player's username
    private String thisUsername;

    // The opponent's username
    private String opponentUsername;

    // The tile rack at the bottom of the screen
    private final TileRack rack;

    // The tile that the player is currently dragging
    private static Tile draggedTile = null;


    public GameScreen(String thisUsername, String opponentUsername, Tile[] tiles, boolean thisTurn) {
        this.thisUsername = thisUsername;
        this.opponentUsername = opponentUsername;

        rack = new TileRack(tiles);
        this.thisTurn = thisTurn;
    }

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float boardEnd = Board.Y + Board.SIZE * (Board.TILE_SIZE + Board.TILE_GAPS);
        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        // Draw the board
        board.draw(graphics);
        // Draw the rack
        rack.draw(graphics);
        drawScores(graphics);
        graphics.fill(255);
        graphics.textAlign(CENTER);
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
        // If the player is dragging a tile draw it on the screen
        if(draggedTile != null) {
            // TODO: do not do this and write a Tile.draw() method.
            graphics.noStroke();
            graphics.fill(242, 173, 26);
            graphics.rect(x, y, TILE_SIZE, TILE_SIZE, Board.TILE_RADIUS);
            graphics.fill(0);
            graphics.textSize(TILE_SIZE * 0.7f);
            graphics.text(draggedTile.getLetter(), textX, textY);
        }
    }

    // Draw the player's scores and usernames on the board
    private void drawScores(PGraphics graphics) {
        graphics.fill(255);
        graphics.textAlign(LEFT);
        graphics.textSize(20);
        // Draw this username
        graphics.text(thisUsername, SCORES_PADDING, 25);
        graphics.textSize(30);
        // Draw this score
        graphics.text(thisScore, SCORES_PADDING, Board.Y - 5);
        graphics.textAlign(RIGHT);
        // Draw the opponent's username
        graphics.textSize(20);
        graphics.text(opponentUsername, Scrabble.WINDOW_WIDTH - SCORES_PADDING, 25);
        // Draw the opponent's score
        graphics.textSize(30);
        graphics.text(opponentScore, Scrabble.WINDOW_WIDTH - SCORES_PADDING, Board.Y - 5);
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            // Our turn was validated by the server and therefore we can finalize it on the board and add points to our
            // score
            case "turn_success": {
                thisTurn = false;
                thisScore += board.checkWordPlacement().points;
                board.finalizeTurn();
                Tile[] newTiles = Parsing.parseTiles(data[0]);
                for(Tile tile : newTiles) {
                    rack.add(tile);
                }
                return true;
            }

            // Our turn was deemed bad by the server so we can't really do much but we'll log it for now
            case "turn_fail": {
                // TODO: i don't think there's too much we can do here to notify the user, i mean it's not their fault
                System.out.println("Failed submitting seemingly valid turn: " + data[0]);
                return true;
            }

            // The opponent submitted a turn and the server sent it to us so we need to place it on the board
            case "opponent_turn": {
                // It's our turn now
                thisTurn = true;
                // Get the amount of points it was worth and add it to the score
                int points = Integer.parseInt(data[0]);
                opponentScore += points;
                // Give the board the tiles and have it add them
                board.applyOpponentTurn(data);
                return true;
            }

            default: {
                return false;
            }
        }
    }

    //
    @Override
    public void mousePressed(int mouseButton) {
        if(mouseButton != LEFT) return;
        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;

        Tile startDrag; // The tile that should start being dragged
        if(mouseY < Scrabble.getWindow().width + Board.Y && mouseY > Board.Y) {
            // Try dragging a tile from the board
            startDrag = board.tryDrag(mouseX, mouseY);
        } else {
            // Try dragging a tile from the rack
            startDrag = rack.tryDrag(mouseX, mouseY);
        }

        // If the user started dragging a tile from either the rack or the board, set draggedTile, so we can render it
        // and keep track of it
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
            // Start dragging the tile if possible
            if(mouseY < Scrabble.getWindow().width + Board.Y && mouseY > Board.Y) {
                if(!board.tryDrop(mouseX, mouseY, draggedTile)) {
                    // If the tile could not be dropped onto the board, add it to the rack
                    rack.add(draggedTile);
                }
            } else {
                // Drop the tile into the rack
                rack.drop(mouseX, mouseY, draggedTile);
            }

            // Set the dragged tile back to null so the user doesn't keep dragging the tile
            draggedTile = null;
        } else if(thisTurn && board.checkWordPlacement().isValid && mouseX > screenCenter - 80 && mouseX < screenCenter + 80 && mouseY > boardEnd + 5 && mouseY < boardEnd + 40) {
            // If the user clicks the "PLAY" button, send the turn to the server
            Scrabble.sendMessage(board.getTurnMessage());
        }
    }
}