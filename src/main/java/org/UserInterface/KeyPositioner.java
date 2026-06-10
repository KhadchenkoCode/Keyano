package org.UserInterface;

import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;

public class KeyPositioner {

    public static final int NEW_ROW_OFFSET = 20;
    public static final int COLUMN_WIDTH = 60;
    public static final int ROW_WIDTH = 60;

    /**
     * Converts a grid position into screen coordinates.
     */
    public static Point2D positionFromOffset(KeyGridPosition position) {
        int column = position.col();
        int row = position.row();

        int x = column* COLUMN_WIDTH+ NEW_ROW_OFFSET * row;
        int y = row * ROW_WIDTH;

        return new Point2D.Double(x, y);
    }

    /**
     * Converts an AWT KeyEvent key code into coordinates.
     */
    public static Point2D positionFromKeyCode(int keyCode) {
        KeyGridPosition position = getGridPosition(keyCode);

        if (position == null) {
            System.out.println("error int keyCode = "+ keyCode +" nullPosition");
            return null;
        }

        return positionFromOffset(position);
    }

    /**
     * Maps AWT/Swing key codes to keyboard grid positions.
     */
    public static KeyGridPosition getGridPosition(int keyCode) {

        switch (keyCode) {

            // Top row
            case KeyEvent.VK_Q: return new KeyGridPosition(0, 1);
            case KeyEvent.VK_W: return new KeyGridPosition(0, 2);
            case KeyEvent.VK_E: return new KeyGridPosition(0, 3);
            case KeyEvent.VK_R: return new KeyGridPosition(0, 4);
            case KeyEvent.VK_T: return new KeyGridPosition(0, 5);
            case KeyEvent.VK_Y: return new KeyGridPosition(0, 6);
            case KeyEvent.VK_U: return new KeyGridPosition(0, 7);
            case KeyEvent.VK_I: return new KeyGridPosition(0, 8);
            case KeyEvent.VK_O: return new KeyGridPosition(0, 9);
            case KeyEvent.VK_P: return new KeyGridPosition(0, 10);

            // Home row
            case KeyEvent.VK_A: return new KeyGridPosition(1, 1);
            case KeyEvent.VK_S: return new KeyGridPosition(1, 2);
            case KeyEvent.VK_D: return new KeyGridPosition(1, 3);
            case KeyEvent.VK_F: return new KeyGridPosition(1, 4);
            case KeyEvent.VK_G: return new KeyGridPosition(1, 5);
            case KeyEvent.VK_H: return new KeyGridPosition(1, 6);
            case KeyEvent.VK_J: return new KeyGridPosition(1, 7);
            case KeyEvent.VK_K: return new KeyGridPosition(1, 8);
            case KeyEvent.VK_L: return new KeyGridPosition(1, 9);

            // Bottom row
            case KeyEvent.VK_BACK_SLASH: return new KeyGridPosition(2, 0);
            case KeyEvent.VK_Z: return new KeyGridPosition(2, 1);
            case KeyEvent.VK_X: return new KeyGridPosition(2, 2);
            case KeyEvent.VK_C: return new KeyGridPosition(2, 3);
            case KeyEvent.VK_V: return new KeyGridPosition(2, 4);
            case KeyEvent.VK_B: return new KeyGridPosition(2, 5);
            case KeyEvent.VK_N: return new KeyGridPosition(2, 6);
            case KeyEvent.VK_M: return new KeyGridPosition(2, 7);

            case KeyEvent.VK_COMMA: return new KeyGridPosition(2, 8);
            case KeyEvent.VK_PERIOD: return new KeyGridPosition(2, 9);
            case KeyEvent.VK_SLASH: return new KeyGridPosition(2, 10);

            // Extra symbols
            case KeyEvent.VK_OPEN_BRACKET: return new KeyGridPosition(0, 11);
            case KeyEvent.VK_CLOSE_BRACKET: return new KeyGridPosition(0, 12);

            case KeyEvent.VK_SEMICOLON: return new KeyGridPosition(1, 10);
            case KeyEvent.VK_QUOTE: return new KeyGridPosition(1, 11);
        }

        System.out.println("return null on keycode " + keyCode);
        return null;
    }
}