package scrabble;

import processing.core.PGraphics;

import static processing.core.PConstants.*;

public class MenuScreen implements Screen {
    private static String username = "";
    private String errorMessage = "";
    private boolean editingUsername = false;

    private static final float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
    private static final float dieX = screenCenter - 75;
    private static final float dieY = 350;
    private static final float dieSideLength = screenCenter - 100;

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;

        graphics.background(Color.MENU_COLOR.r, Color.MENU_COLOR.g, Color.MENU_COLOR.b);
        graphics.fill(255, 61, 61);
        graphics.textAlign(CENTER);
        graphics.textSize(20);
        if(!errorMessage.isEmpty()) {
            graphics.text(errorMessage, screenCenter, 175);
        }
        graphics.textSize(30);
        graphics.fill(255);
        graphics.stroke(0);

        graphics.text("Enter a username:", screenCenter, 60);
        graphics.rect(screenCenter - 100, 100, 200, 40);
        graphics.fill(0);
        graphics.textAlign(LEFT);
        graphics.text(username, screenCenter - 95, 100 + 40 * 0.8f);
        if(editingUsername) {
            graphics.rect(screenCenter - 94 + graphics.textWidth(username), 105, 3, 30);
        }
        graphics.textAlign(CENTER);
        graphics.fill(255);
        graphics.stroke(255);
        graphics.text("Random Game", screenCenter, 300);

        // Draw the die icon
        graphics.stroke(0); // Set outline color to black
        graphics.rect(dieX, dieY, dieSideLength, dieSideLength, 20);

        // Draw dots to represent "5" on the die side without an outline
        graphics.fill(0);  // Set dot color to black
        graphics.noStroke(); // Disable dot outline

        float dotSize = 20;  // Size of each dot
        float dotSpacing = (dieSideLength) / 4.0f;  // Spacing between dots

        // Calculate the positions of the dots
        float dotX = dieX + dotSpacing;
        float dotY = dieY + dotSpacing;

        // Draw the dots
        graphics.ellipse(dotX, dotY, dotSize, dotSize);  // Top-left dot
        graphics.ellipse(dotX + 2 * dotSpacing, dotY, dotSize, dotSize);  // Top-right dot
        graphics.ellipse(dotX + dotSpacing, dotY + dotSpacing, dotSize, dotSize);  // Center dot
        graphics.ellipse(dotX, dotY + 2 * dotSpacing, dotSize, dotSize);  // Bottom-left dot
        graphics.ellipse(dotX + 2 * dotSpacing, dotY + 2 * dotSpacing, dotSize, dotSize);  // Bottom-right dot
    }

    @Override
    public boolean handleMessage(String type, String[] data) {
        switch(type) {
            case "random_waiting": {
                Scrabble.changeScreen(new WaitingScreen(username));
                return true;
            }
            case "random_game_start": {
                String opponent = data[0];
                Tile[] tiles = Parsing.parseTiles(data[1]);
                boolean thisTurn = Boolean.parseBoolean(data[2]);
                Scrabble.changeScreen(new GameScreen(username, opponent, tiles, thisTurn));
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
        if(mouseX >= dieSideLength && mouseX <= screenCenter + 100 && mouseY >= 100 && mouseY <= 140) {
            editingUsername = true;
        } else if(mouseX >= dieX && mouseX <= dieX + dieSideLength && mouseY >= dieY && mouseY <= dieY + dieSideLength) {
            if(username.isEmpty()) {
                errorMessage = "Error: No username";
            } else {
                Scrabble.sendMessage("random:" + username);
            }
        } else {
            editingUsername = false;
        }
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        boolean isValidChar = Character.isAlphabetic(key) || Character.isDigit(key) || key == ' ';

        if(editingUsername) {
            if(isValidChar) {
                username += key;
            } else if(key == '\b') {
                if(username.length() > 0) {
                    username = username.substring(0, username.length() - 1);
                }
            }
        }
    }
}
