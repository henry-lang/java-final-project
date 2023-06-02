package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;

public class WelcomeScreen implements Screen {
    @Override
    public void onFrame(PGraphics graphics) {
        // TODO: stop hardcoding here too
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        // Render the welcome message
        graphics.fill(255);
        graphics.textAlign(CENTER);
        graphics.textSize(50);
        graphics.text("Phrases With Phriends", screenCenter, 100);
        graphics.textSize(20);
        graphics.text("By Henry Langmack and Ayush Shrivastava", screenCenter, 150);

        // Render the button
        graphics.rect(screenCenter - 80, 300, 160, 80, 20);
        graphics.textSize(50);
        graphics.fill(0);
        graphics.text("PLAY", screenCenter, 300 + 80 * 0.7f);
    }

    @Override
    public void mouseReleased(int mouseButton) {
        if(mouseButton != LEFT) return;

        float mouseX = Scrabble.getWindow().mouseX;
        float mouseY = Scrabble.getWindow().mouseY;
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        if(mouseX > screenCenter - 80 && mouseX < screenCenter + 80 && mouseY > 300 && mouseY < 380) {
            Scrabble.changeScreen(new MenuScreen());
        }
    }
}
