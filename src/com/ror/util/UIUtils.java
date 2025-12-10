package com.ror.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class UIUtils {

    private UIUtils() {}

    public static void styleLargeButton(JButton b, Font f) {

        Color NBorder = Color.WHITE;
        Color HBorder = Color.DARK_GRAY;
        Color outline = Color.WHITE;

        b.setFont(f);
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);

        b.setFocusPainted(false);
        b.setContentAreaFilled(false);

        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(outline, 2),
            new EmptyBorder(12, 28, 12, 28)
        ));

         b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(HBorder, 2),
                    new EmptyBorder(12, 28, 12, 28)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(NBorder, 2),
                    new EmptyBorder(12, 28, 12, 28)
                ));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                Sound.play("/com/ror/model/Assets/sfx/BattleClick.wav");
            }
        });
        
        b.setPreferredSize(new Dimension(220, 64));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleIconButton(JButton b) {

        Color NBorder = Color.WHITE;
        Color HBorder = Color.DARK_GRAY;
        Color outline = Color.WHITE;

        b.setFont(GameFonts.pixelFont.deriveFont(16f));
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);

        b.setContentAreaFilled(false);
        b.setFocusPainted(false);

        b.setBorder(new LineBorder(outline, 2));
        b.setPreferredSize(new Dimension(36, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

         b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(HBorder, 2),
                    new EmptyBorder(12, 28, 12, 28)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(NBorder, 2),
                    new EmptyBorder(12, 28, 12, 28)
                ));
            }
        }); 
    }
}
