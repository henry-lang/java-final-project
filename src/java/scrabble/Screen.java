package scrabble;

import processing.core.PGraphics;

public interface Screen {
    // Will return a new screen if the current screen should be replaced.
    Screen onFrame(PGraphics graphics);
    void handleServerMessage();
    void mousePressed(int mouseButton);
    void mouseReleased(int mouseButton);
    void keyPressed();
}
