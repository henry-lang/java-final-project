package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;

// This screen represents the screen which shows up when a player has won or lost the game
public class GameEndScreen implements Screen {
    // Whether this player has won
    private final boolean won;

    // Whether the opponent left which caused the game to end
    private final boolean opponentLeft;

    // String formatted --> thisScore + " - " + opponentScore
    private final String scoreString;
    public GameEndScreen(boolean won, boolean opponentLeft, int thisScore, int opponentScore) {
        this.won = won;
        this.opponentLeft = opponentLeft;
        this.scoreString = thisScore + " - " + opponentScore;
    }

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        graphics.fill(255);
        graphics.textAlign(CENTER);
        graphics.textSize(50);
        // Won/lost text
        if(won) {
            graphics.text("You won!", screenCenter, 100);
        } else {
            graphics.text("You lost!", screenCenter, 100);
        }

        if(opponentLeft) {
            graphics.textSize(20);
            graphics.text("Opponent left", screenCenter, 150);
        }

        graphics.textSize(40);
        graphics.text(scoreString, screenCenter, 225);

        // Render the button to return back to menu
        graphics.rect(screenCenter - 175, 300, 350, 80, 20);
        graphics.textSize(50);
        graphics.fill(0);
        graphics.text("BACK TO MENU", screenCenter, 300 + 80 * 0.7f);
    }

    @Override
    public void mouseReleased(int mouseButton) {
        if(mouseButton != LEFT) return;

        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        // Menu button click test
        if(mouseX > screenCenter - 175 && mouseX < screenCenter + 175 && mouseY > 300 && mouseY < 380) {
            Scrabble.changeScreen(new MenuScreen());
        }
    }
}
