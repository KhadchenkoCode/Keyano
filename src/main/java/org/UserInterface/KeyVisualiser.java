package org.UserInterface;


import java.awt.*;
import java.awt.geom.Point2D;

import org.example.NotePlayer;

public class KeyVisualiser {
    public String text;
    public Rectangle bounds;
    float brightness;

    private final Font defaultFont = new Font("SansSerif", Font.BOLD, 12);

    void draw (Graphics g) {

        //double normalisation = 1+ NotePlayer.valueAt005;

        int color = (int)(brightness*255/1.85);
        Color c = new Color(color, color, color);
        Color c1=g.getColor();

        g.setColor(c);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g.setColor(Color.red);
        g.setFont(defaultFont);
        g.drawString(text, bounds.x +8, bounds.y +12);
    }

    public static Rectangle rectangleFromPoint(Point2D p) {
        return new Rectangle((int)p.getX(), (int)p.getY(), 40, 40);
    }

}
