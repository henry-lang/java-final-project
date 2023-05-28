package scrabble;

import processing.core.PGraphics;

public interface Screen {
    void onFrame(PGraphics graphics);
    default void handleServerMessage() {}
    default void mousePressed(int mouseButton) {}
    default void mouseReleased(int mouseButton) {}
    default void keyPressed() {}
}
