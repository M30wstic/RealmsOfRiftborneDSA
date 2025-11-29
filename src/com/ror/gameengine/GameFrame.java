package com.ror.gameengine;

import com.ror.gamemodel.*;
import java.awt.*;
import javax.swing.*;

public class GameFrame extends JFrame {
    private MenuPanel menuPanel;
    private CharacterSelectPanel selectPanel;
    private BattlePanel battlePanel;

    public GameFrame() {
        setTitle("Realms of Riftborne");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new CardLayout());

        menuPanel = new MenuPanel(this);
        selectPanel = new CharacterSelectPanel(this);
        battlePanel = new BattlePanel(this);

        getContentPane().add(menuPanel, "Menu");
        getContentPane().add(selectPanel, "Select");
        getContentPane().add(battlePanel, "Battle");

        showMenu();
        setVisible(true);
    }

    public void showMenu() {
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Menu");
    }

    public void showSelect() {
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Select");
    }

    public void showBattle(Entity chosenCharacter) {
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "Battle");
        battlePanel.startBattle(chosenCharacter);
    }
}