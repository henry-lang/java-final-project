package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;

public class MenuScreen implements Screen {
    private String username = "";
    private String gameCode = "";
    private String errorMessage = "";

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float screenHeight = Scrabble.WINDOW_HEIGHT;

        graphics.background(66, 170, 255);
        graphics.fill(255);
        graphics.stroke(0);
        graphics.textAlign(CENTER);
        graphics.textSize(30);

        graphics.text("Enter a username:", screenCenter, 60);
        graphics.rect(screenCenter - 100, 100, 200, 40);
        graphics.stroke(255);
        graphics.line(screenCenter, 200, screenCenter, screenHeight);
        graphics.text("Random Game", screenCenter / 2, 300);
        graphics.text("Private Game", screenCenter + screenCenter / 2, 300);

        // Draw the die icon
        graphics.stroke(0); // Set outline color to black
        graphics.rect(50, 350, screenCenter - 100, screenCenter - 100, 20);

        // Draw dots to represent "5" on the die side without an outline
        graphics.fill(0);  // Set dot color to black
        graphics.noStroke(); // Disable dot outline

        float dotSize = 20;  // Size of each dot
        float dotSpacing = (screenCenter - 100) / 4.0f;  // Spacing between dots

        // Calculate the positions of the dots
        float dotX = 50 + dotSpacing;
        float dotY = 350 + dotSpacing;

        // Draw the dots
        graphics.ellipse(dotX, dotY, dotSize, dotSize);  // Top-left dot
        graphics.ellipse(dotX + 2 * dotSpacing, dotY, dotSize, dotSize);  // Top-right dot
        graphics.ellipse(dotX + dotSpacing, dotY + dotSpacing, dotSize, dotSize);  // Center dot
        graphics.ellipse(dotX, dotY + 2 * dotSpacing, dotSize, dotSize);  // Bottom-left dot
        graphics.ellipse(dotX + 2 * dotSpacing, dotY + 2 * dotSpacing, dotSize, dotSize);  // Bottom-right dot

        // Draw the "Create" button
        graphics.rect(screenCenter + 50, 250, 100, 40);
        graphics.textSize(16);
        graphics.fill(255);
        graphics.textAlign(CENTER, CENTER);
        graphics.text("Create", screenCenter + 100, 270);

        // Draw the "Enter game code" textbox
        graphics.rect(screenCenter + 50, 320, 200, 40);
        graphics.fill(0);
        graphics.textSize(16);
        graphics.textAlign(CENTER, CENTER);
        graphics.text("Enter game code", screenCenter + 150, 340);

        // Draw the "Join" button
        graphics.rect(screenCenter + 50, 380, 100, 40);
        graphics.textSize(16);
        graphics.fill(255);
        graphics.textAlign(CENTER, CENTER);
        graphics.text("Join", screenCenter + 100, 400);
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            case "random_game_found": {
                Scrabble.changeScreen(new GameScreen());
                return true;
            }
            case "create_fail": {
                errorMessage = data[1];
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
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float screenHeight = Scrabble.WINDOW_HEIGHT;

        if(mouseX >= 50 && mouseX <= screenCenter - 50 && mouseY >= 350 && mouseY <= 250 + screenCenter) {
            Scrabble.sendMessage("random");
        }
    }
}
