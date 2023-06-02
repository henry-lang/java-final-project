package scrabble;

import processing.core.PGraphics;

public class RandomWaitingScreen implements Screen {
    @Override
    public void onFrame(PGraphics graphics) {
        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        graphics.text("Waiting", 100, 100);
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            case "random_game_found": {
                Scrabble.changeScreen(new GameScreen());
                return true;
            }
            default: {
                return false;
            }
        }
    }
}
