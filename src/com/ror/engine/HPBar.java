package com.ror.engine;

import java.awt.*;
import javax.swing.JPanel;

public class HPBar extends JPanel{

  private static Font pixelFont;

    static {
        try {
            pixelFont = Font.createFont(
                Font.TRUETYPE_FONT, 
                BattlePanel.class.getResourceAsStream(
                    "/com/ror/model/assets/fonts/bytebounce.medium.ttf"
                    )
                ).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  private int currentHP;
  private int maxHP;

  public HPBar(int currentHP, int maxHP) {
    this.maxHP = maxHP;
    this.currentHP = currentHP;
    this.setPreferredSize(new Dimension(200, 22));
    setOpaque(false);
  }

  public void updateHP(int currentHP, int maxHP) {
    this.currentHP = currentHP;
    this.maxHP = maxHP;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth();
    int height = getHeight();

    double hpPercent = Math.max(0, (double)currentHP/maxHP);
    int filledWidth = (int)((width - 4) * hpPercent);

    //Outer Border this
    g2.setColor(new Color(40, 40, 40));
    g2.fillRoundRect(0, 0, width, height, 10, 10);

    //Background this
    g2.setColor(Color.BLACK);
    g2.fillRoundRect(2, 2, width - 4, height - 4, 8, 8);

    //HP color, omit for white HP
    // if(hpPercent > 0.6) {
    //   g2.setColor(new Color(0, 200, 0));
    // } else if(hpPercent > 0.3) {
    //   g2.setColor(new Color(255, 180, 0));
    // } else {
    //   g2.setColor(new Color(220, 0, 0));
    // }

    //Omit for colored vers V
    g2.setColor(Color.WHITE);
    g2.fillRoundRect(2, 2, filledWidth, height - 4, 8, 8);

    //highlight
    // g2.setColor(new Color(255, 255, 255, 80));
    g2.setColor(Color.WHITE); //Omit if Colored ganahan
    g2.fillRoundRect(2, 2, filledWidth, (height - 4) / 2, 8, 8);

    //Outline
    g2.setColor(Color.WHITE);
    g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

    //Optional ni for text overlay in HP bar
    //In case you wanna transfer HP string inside it

    // String hpText = "HP" + currentHP + " / " + maxHP;
    // g2.setColor(Color.WHITE);
    // g2.setFont(pixelFont.deriveFont(16f));

    // FontMetrics fm = g2.getFontMetrics();
    // int textX = (width - fm.stringWidth(hpText)) / 2;
    // int textY = (height + fm.getAscent()) / 2 - 2;

    // g2.drawString(hpText, textX, textY);
  }
}
