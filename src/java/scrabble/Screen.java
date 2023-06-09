package scrabble;

import processing.core.PGraphics;

// The base screen interface which lists all of the requirements for a screen on the app
public interface Screen {
    // Every frame, this method will be called and the PGraphics will be passed in as a means of rendering.
    void onFrame(PGraphics graphics);

    // This method returns whether the screen supports handling this message,
    // returning false signifies that it isn't appropriate for the current context
    // of the app. Implementors of this method should switch on the "type" and handle
    // the message accordingly.
    default boolean handleMessage(String type, String[] data) {
        return false;
    }

    // This method is called whenever the user presses the mouse button
    default void mousePressed(int mouseButton) {}

    // This method is called whenever the user releases the mouse button
    default void mouseReleased(int mouseButton) {}

    // This method called whenever the user presses a key, and the key character and the int keycode are passed in
    default void keyPressed(char key, int keyCode) {}
}
