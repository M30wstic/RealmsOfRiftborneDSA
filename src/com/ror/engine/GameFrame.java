package com.ror.engine;

import com.ror.model.*;
import com.ror.util.MusicController;
import java.awt.*;
import javax.swing.*;

public class GameFrame extends JFrame {
    private MenuPanel menuPanel;
    private CharacterSelectPanel selectPanel;
    private BattlePanel battlePanel;

    public GameFrame() {
        setTitle("Realms of Riftborne");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

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
        MusicController.play("/com/ror/model/Assets/sfx/Main.ogg", true);
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