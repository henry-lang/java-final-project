package scrabble;

import processing.core.PGraphics;

public interface Screen {
    void onFrame(PGraphics graphics);

    // This method returns whether the screen supports handling this message,
    // returning false signifies that it isn't appropriate for the current context
    // of the app.
    default boolean handleMessage(String type, String[] data) {
        return false;
    }

    default void mousePressed(int mouseButton) {}
    default void mouseReleased(int mouseButton) {}
    default void keyPressed() {}
}
