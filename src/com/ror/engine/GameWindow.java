package com.ror.engine;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.*;

public class GameWindow {

    private JFrame frame;
    private GamePanel gamePanel;

    public GameWindow() {
        frame = new JFrame("My Game");
        gamePanel = new GamePanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void setFullscreen(boolean fullscreen) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        frame.dispose();
        frame.setUndecorated(fullscreen);

        if (fullscreen) {
            frame.setSize(1280, 720);
            device.setFullScreenWindow(frame); // FULLSCREEN WINDOWED
        } else {
            device.setFullScreenWindow(null);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
        }

        frame.setVisible(true);
    }
}

