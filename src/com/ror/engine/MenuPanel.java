package com.ror.engine;

import com.ror.util.GameFonts;
import com.ror.util.MusicController;
import com.ror.util.UIUtils;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MenuPanel extends JPanel {
    public final GameFrame parent;
    private JButton playBtn, exitBtn;
    private Image backgroundImage;

    public MenuPanel(GameFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        MusicController.play("/com/ror/model/Assets/sfx/Main.ogg", true);

        // Title
        JLabel title = new JLabel("Realms Of Riftborne", SwingConstants.CENTER);
        title.setFont(GameFonts.pixelFont.deriveFont(64f));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(150, 10, 20, 10)); // move title down

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(title, BorderLayout.CENTER);
        add(titleWrap, BorderLayout.NORTH);

        // Buttons
        playBtn = new JButton("Play");
        exitBtn = new JButton("Exit");

        UIUtils.styleLargeButton(playBtn, GameFonts.pixelFont.deriveFont(28f));
        UIUtils.styleLargeButton(exitBtn, GameFonts.pixelFont.deriveFont(28f));

        playBtn.addActionListener(e -> parent.showSelect());
        exitBtn.addActionListener(e -> System.exit(0));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 60, 0, 60);
        gbc.gridy = 0;
        gbc.gridx = 0;

        center.add(playBtn, gbc);
        gbc.gridx = 1;
        center.add(exitBtn, gbc);

        add(center, BorderLayout.CENTER);

        // Footer panel (kept but will be drawn inside bottom box visually)
        JLabel rfooter = new JLabel("Debug Build V1.0", SwingConstants.RIGHT);
        rfooter.setFont(GameFonts.pixelFont.deriveFont(24f));
        rfooter.setForeground(Color.WHITE);
        rfooter.setBorder(new EmptyBorder(0, 20, 8, 20));

        JLabel lfooter = new JLabel("Inspired from STICK STORY RPG & UNDERTALE", SwingConstants.LEFT);
        lfooter.setFont(GameFonts.pixelFont.deriveFont(24f));
        lfooter.setForeground(Color.WHITE);
        lfooter.setBorder(new EmptyBorder(0, 20, 8, 20));

        JPanel fp = new JPanel(new BorderLayout());
        fp.setOpaque(false);
        fp.add(rfooter, BorderLayout.EAST);
        fp.add(lfooter, BorderLayout.WEST);

        add(fp, BorderLayout.SOUTH);

        // Load background
        setBackgroundImage("/com/ror/model/Assets/Backgrounds/Riftborne.png");
    }

    public void setBackgroundImage(String path) {
        try {
            backgroundImage = ImageIO.read(getClass().getResource(path));
            repaint();
        } catch (Exception e) {
            System.out.println("Could not load background: " + path);
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background (keeps aspect ratio)
        if (backgroundImage != null) {
            int imgW = backgroundImage.getWidth(null);
            int imgH = backgroundImage.getHeight(null);
            double imgAspect = (double) imgW / imgH;
            double panelAspect = (double) getWidth() / getHeight();

            int drawW, drawH;
            if (panelAspect > imgAspect) {
                drawH = getHeight();
                drawW = (int) (drawH * imgAspect);
            } else {
                drawW = getWidth();
                drawH = (int) (drawW / imgAspect);
            }
            int x = (getWidth() - drawW) / 2;
            int y = (getHeight() - drawH) / 2;
            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
        }

        // draw cinematic bars
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));

        // TOP BAR
        int topBarHeight = 70;  // matches your screenshot
        int topY = topBarHeight;
        g2.drawLine(0, topY, getWidth(), topY);

        // BOTTOM CINEMATIC BAR — ONLY top border
        int bottomBarHeight = 70;
        int bottomY = getHeight() - bottomBarHeight;
        g2.drawLine(0, bottomY, getWidth(), bottomY);

        g2.dispose();
    }

}
