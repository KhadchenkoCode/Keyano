package org.UserInterface;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class KeyVisualiser {
    public String text;
    public Rectangle bounds;
    float brightness;
    private final Font defaultFont = new Font("SansSerif", 1, 12);

    void draw(Graphics g) {
        int color = (int)((double)(this.brightness * 255.0F) / 1.85);
        Color c = new Color(color, color, color);
        Color c1 = g.getColor();
        g.setColor(c);
        g.drawRect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
        g.fillRect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
        g.setColor(Color.red);
        g.setFont(this.defaultFont);
        g.drawString(this.text, this.bounds.x + 8, this.bounds.y + 12);
    }

    public static Rectangle rectangleFromPoint(Point2D p) {
        return new Rectangle((int)p.getX(), (int)p.getY(), 40, 40);
    }
}