package com.ror.engine;

import java.awt.*;
import javax.swing.*;

//unused class for the main game panel
public class GamePanel extends JPanel {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        int screenW = getWidth();
        int screenH = getHeight();

        double scaleX = screenW / (double) WIDTH;
        double scaleY = screenH / (double) HEIGHT;

        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (WIDTH * scale);
        int scaledHeight = (int) (HEIGHT * scale);
        int x = (screenW - scaledWidth) / 2;
        int y = (screenH - scaledHeight) / 2;

        g2.translate(x, y);
        g2.scale(scale, scale);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
