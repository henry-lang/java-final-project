package scrabble;

import processing.core.PGraphics;

import java.util.Random;

import static processing.core.PConstants.*;

public class MenuScreen implements Screen {
    enum SelectedTextBox {
        NONE,
        USERNAME,
        GAME_CODE
    }

    private static String username = "";
    private String gameCode = "";
    private String errorMessage = "";
    private SelectedTextBox selectedTextBox;

    @Override
    public void onFrame(PGraphics graphics) {
        float screenCenter = Scrabble.WINDOW_WIDTH / 2.0f;
        float screenHeight = Scrabble.WINDOW_HEIGHT;

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
        if(selectedTextBox == SelectedTextBox.USERNAME) {
            graphics.rect(screenCenter - 94 + graphics.textWidth(username), 105, 3, 30);
        }
        graphics.textAlign(CENTER);
        graphics.fill(255);
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
            case "random_waiting": {
                Scrabble.changeScreen(new RandomWaitingScreen());
                return true;
            }
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
        if(mouseX >= screenCenter - 100 && mouseX <= screenCenter + 100 && mouseY >= 100 && mouseY <= 140) {
            selectedTextBox = SelectedTextBox.USERNAME;
        } else if(mouseX >= 50 && mouseX <= screenCenter - 50 && mouseY >= 350 && mouseY <= 250 + screenCenter) {
            if(username.isEmpty()) {
                errorMessage = "Error: No username";
            } else {
                Scrabble.sendMessage("random");
            }
        } else {
            selectedTextBox = SelectedTextBox.NONE;
        }
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        boolean isValidChar = Character.isAlphabetic(key) || Character.isDigit(key) || key == ' ';

        switch(selectedTextBox) {
            case USERNAME: {
                if(isValidChar) {
                    username += key;
                } else if(key == '\b') {
                    if(username.length() > 0) {
                        username = username.substring(0, username.length() - 1);
                    }
                }
                break;
            }
            case GAME_CODE: {
                if(isValidChar) {
                    gameCode += key;
                } else if(key == '\b') {
                    if(gameCode.length() > 0) {
                        gameCode = gameCode.substring(0, gameCode.length() - 1);
                    }
                }
                break;
            }
        }
    }
}
