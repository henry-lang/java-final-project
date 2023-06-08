package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.CENTER;

public class RandomWaitingScreen implements Screen {
    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        graphics.textAlign(CENTER);
        graphics.fill(255);
        graphics.textSize(30);
        graphics.text("Waiting for other player to join...", screenCenter, 100);

        graphics.stroke(0);
        // Render the button
        graphics.rect(screenCenter - 80, 300, 160, 80, 20);
        graphics.textSize(35);
        graphics.fill(0);
        graphics.text("CANCEL", screenCenter, 300 + 80 * 0.65f);
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            case "random_game_start": {
                String opponent = data[0];
                Tile[] tiles = Parsing.parseTiles(data[1]);
                boolean thisTurn = Boolean.parseBoolean(data[2]);
                Scrabble.changeScreen(new GameScreen(opponent, tiles, thisTurn));
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public void mousePressed(int button) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;

        if(mouseX > screenCenter - 80 && mouseX < screenCenter + 80 && mouseY > 300 && mouseY < 380) {
            Scrabble.sendMessage("random_cancel");
            Scrabble.changeScreen(new MenuScreen());
        }
    }
}
