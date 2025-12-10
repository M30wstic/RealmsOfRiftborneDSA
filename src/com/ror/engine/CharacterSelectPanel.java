package com.ror.engine;

import com.ror.util.GameFonts;
import com.ror.util.Sound;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;


public class CharacterSelectPanel extends JPanel {
    private JButton backButton;
    private JButton andrewButton;
    private JButton flameWarriorButton;
    private JButton skyMageButton;
    private JButton NyxButton;    
    private JButton TharnButton; 
    private Image backgroundImage;


    public GameFrame parent;

    public CharacterSelectPanel(GameFrame parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(Color.BLACK);

       setBackgroundImage("/com/ror/model/Assets/Backgrounds/Riftborne.png");


        // Title 
        JLabel title = new JLabel("Select Your Character", SwingConstants.CENTER);
        title.setFont(GameFonts.pixelFont.deriveFont(36f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 40, 1280, 60);
        add(title);

        addCharacterBox("Andrew ( Time Blade )", "/com/ror/model/Assets/Images/Andrew.png",
                140, 160, e -> parent.showBattle(new com.ror.model.Playable.Andrew()));

        addCharacterBox("Flashey ( Sky Mage )", "/com/ror/model/Assets/Images/Flashley.png",
                740, 160, e -> parent.showBattle(new com.ror.model.Playable.SkyMage()));

        addCharacterBox("Drax ( Flame Warrior )", "/com/ror/model/Assets/Images/Drax.png",
                140, 340, e -> parent.showBattle(new com.ror.model.Playable.FlameWarrior()));

        addCharacterBox("Nyx ( Assassin )", "/com/ror/model/Assets/Images/Nyx.png",
                740, 340, e -> parent.showBattle(new com.ror.model.Playable.Nyx()));

        addCharacterBox("Tharn ( Stone Golem )", "/com/ror/model/Assets/Images/Tharn.png",
                440, 520, e -> parent.showBattle(new com.ror.model.Playable.Tharn()));

        JButton back = new JButton();
        back.setBounds(1180, 35, 60, 60);
        back.setContentAreaFilled(false);
        back.setBorder(null);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //setIcon
        back.addActionListener(e -> parent.showMenu());
        add(back);

    }

   private void addCharacterBox(String name, String path, int x, int y, ActionListener clickAction) {

        JPanel box = new JPanel(null);
        box.setBounds(x, y, 400, 120);
        box.setBackground(Color.BLACK);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));

        box.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
                box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.actionPerformed(null);
                Sound.play("/com/ror/model/Assets/sfx/BtnClick.wav");
            }
        });

        int spriteX = 20, spriteY = 12, spriteW = 96, spriteH = 96;
        JLabel sprite;
        try {
            ImageIcon icon = loadScaledIcon(path, spriteW, spriteH);
            sprite = new JLabel(icon);
            // center inside the 96x96 cell so image isnt a clusterfuck
            int imgW = icon.getIconWidth();
            int imgH = icon.getIconHeight();
            int offsetX = spriteX + (spriteW - imgW) / 2;
            int offsetY = spriteY + (spriteH - imgH) / 2;
            sprite.setBounds(offsetX, offsetY, imgW, imgH);
        } catch (Exception ex) {
            // failsafe: placeholder square centered in sprite cell
            sprite = new JLabel();
            sprite.setOpaque(true);
            sprite.setBackground(Color.DARK_GRAY);
            sprite.setBounds(spriteX + 10, spriteY + 10, spriteW - 20, spriteH - 20);
        }
        box.add(sprite);

        JLabel label = new JLabel(name);
        label.setForeground(Color.WHITE);
        label.setFont(GameFonts.pixelFont.deriveFont(28f));
        label.setBounds(120, 40, 280, 40);
        box.add(label);

        add(box);
   }

   private ImageIcon loadScaledIcon(String path, int w, int h) throws Exception {
    ImageIcon src = new ImageIcon(getClass().getResource(path));
    Image img = src.getImage();
    if(img == null) throw new Exception("Null imo image: " + path);

    int iw = img.getWidth(null);
    int ih = img.getHeight(null);

    if(iw <= 0 || ih <= 0) throw new Exception("Dim imo image: " + path);

    double scale = Math.min((double) w / iw, (double) h / ih);
    int newW = (int) Math.round(iw * scale);
    int newH = (int) Math.round(ih * scale);

    Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    return new ImageIcon(scaled);
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
    }



    // Getter (for flexibility)
    public JButton getAndrewButton() { return andrewButton; }
    public JButton getBackButton() { return backButton; }
    public JButton getFlameWarriorButton() { return flameWarriorButton; }
    public JButton getSkyMageButton() { return skyMageButton; }
    public JButton getNyxButton() { return NyxButton; }
    public JButton getTharnButton() { return TharnButton; }
}
