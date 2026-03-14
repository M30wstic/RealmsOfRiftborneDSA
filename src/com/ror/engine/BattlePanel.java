//BattlePanel is responsible for the display of the battle interface, including the player and enemy stats, battle log, skill buttons, and overlays for story, tutorial, and end screens. It listens to events from the BattleEngine to update the UI accordingly and also captures player input to delegate actions back to the engine.
package com.ror.engine;

import com.ror.model.*;
import com.ror.util.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class BattlePanel extends JPanel {

    public GameFrame parent;
    public JLayeredPane layeredPane;
    private JPanel exitOverlay, storyOverlay, tutorialOverlay, glass;
    private JScrollPane logScroll;
    private JButton backButton;
    private JTextArea battleLog;
    private JButton skillBtn1, skillBtn2, skillBtn3;
    private JLabel playerHPLabel, enemyHPLabel, playerNameLabel, enemyNameLabel;
    private JLabel playerLevelLabel;
    private JLabel storyText, storyContinue, tutorialText, tutorialContinue;

    private JPanel endOverlay;
    private JLabel endText;

    private Timer storyTypeTimer;
    private String storyFullText;
    private boolean isStoryTyping = false;

    public int lastDamageTakenByPlayer = 0;
    private BattleEngine engine;
    private Entity player;
    private Entity enemy;
    private boolean playerTurn = true;

    boolean playerShieldActive = false;
    boolean playerDodgeActive = false;
    private boolean enemyBlinded = false;
    private int delayedDamageToEnemy = 0;
    private int burnDamageToEnemy = 0;
    private int burnTurnsRemaining = 0;
    private int playerShieldTurns;
    private String mode = "Tutorial";
    private boolean storyActive = false, tutorialActive = false;
    private LinkedQueue<String> storyQueue = new LinkedQueue<>();
    private LinkedQueue<String> tutorialQueue = new LinkedQueue<>();

    private HPBar playerHPBar;
    private HPBar enemyHPBar;

    public WorldManager worldManager = new WorldManager();

    public BattlePanel(GameFrame parent) {

        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        engine = new BattleEngine(new BattleEngine.Listener() {
            @Override
            public void onLog(String message) {
                log(message);
            }

            @Override
            public void onUpdateHP(Entity player, Entity enemy) {
                playerNameLabel.setText(player.getName());
                enemyNameLabel.setText(enemy.getName());

                playerHPLabel.setText("HP: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
                playerHPBar.updateHP(player.getCurrentHealth(), player.getMaxHealth());
                playerLevelLabel.setText("Lv: " + player.getLevel());

                enemyHPLabel.setText("HP: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
            }

            @Override
            public void onUpdateSkillButtons(Skill[] skills) {
                skillBtn1.setText(skills[0].getName()
                        + (skills[0].isOnCooldown() ? " (CD: " + skills[0].getCurrentCooldown() + ")" : ""));
                skillBtn2.setText(skills[1].getName()
                        + (skills[1].isOnCooldown() ? " (CD: " + skills[1].getCurrentCooldown() + ")" : ""));
                skillBtn3.setText(skills[2].getName()
                        + (skills[2].isOnCooldown() ? " (CD: " + skills[2].getCurrentCooldown() + ")" : ""));
            }

            @Override
            public void onShowStory(String text) {
                showStoryOverlay(text);
            }

            @Override
            public void onShowTutorial(String text) {
                showTutorial(text);
            }

            @Override
            public void onClearBattleLog() {
                clearBattleLog();
            }

            @Override
            public void onSetBackground(String path) {
                setBackgroundImage(path);
            }

            @Override
            public void onPlayMusic(String path, boolean loop) {
                MusicController.play(path, loop);
            }

            @Override
            public void onStopMusic() {
                Music.stop();
            }

            @Override
            public void onPlaySoundThen(String soundPath, Runnable nextAction) {
                Sound.playThen(soundPath, nextAction);
            }

            @Override
            public void onEnableSkillButtons(boolean enabled) {
                if (enabled) {
                    enableSkillButtons();
                } else {
                    disableSkillButtons();
                }
            }

            @Override
            public void onPlayerTurnPrompt() {
                log("- Your turn! Choose your next skill.");
            }

            @Override
            public void onGameOver() {
                disableSkillButtons();
                showEndOverlay("GAME OVER\nYou have been defeated.\n\nPress any key or click to return to menu.");
            }

            @Override
            public void onGameWin() {
                disableSkillButtons();
                showEndOverlay(
                        "Vorthnar collapses — time itself shatters, then reforms.\nVICTORY!\nYou have defeated Lord Vorthnar!\n\nPress any key or click to return to menu.");
            }
        });

        // TOP BAR (title + icons + enemy box)
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.BLACK);
        topContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        // LEFT GAME TITLE
        JLabel titleLabel = new JLabel("Realms of Riftborne", SwingConstants.LEFT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(GameFonts.pixelFont.deriveFont(18f));
        topContainer.add(titleLabel, BorderLayout.WEST);

        JPanel cornerIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cornerIcons.setBackground(Color.BLACK);

        JButton menuIcon = new JButton("=");

        UIUtils.styleIconButton(menuIcon);

        cornerIcons.add(menuIcon);
        topContainer.add(cornerIcons, BorderLayout.EAST);

        menuIcon.addActionListener(e -> showExitOverlay());

        // Eemy frame
        JPanel enemyOuterFrame = new JPanel(new BorderLayout());
        enemyOuterFrame.setBackground(Color.BLACK);
        enemyOuterFrame.setBorder(new EmptyBorder(10, 200, 10, 200));

        JPanel enemyFrame = new JPanel(new BorderLayout(10, 0));
        enemyFrame.setBackground(Color.BLACK);
        enemyFrame.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 2),
                new EmptyBorder(10, 12, 10, 12)));

        // LEFT — Enemy Name
        enemyNameLabel = new JLabel("Enemy", SwingConstants.CENTER);
        enemyNameLabel.setForeground(Color.WHITE);
        enemyNameLabel.setFont(GameFonts.pixelFont.deriveFont(22f));
        enemyNameLabel.setBorder(new EmptyBorder(0, 6, 0, 6));
        enemyFrame.add(enemyNameLabel, BorderLayout.WEST);

        // CENTER — HP BAR
        enemyHPBar = new HPBar(1, 1);
        enemyHPBar.setPreferredSize(new Dimension(300, 18));

        JPanel enemyCenter = new JPanel(new BorderLayout(6, 0));
        enemyCenter.setBackground(Color.BLACK);
        enemyCenter.add(enemyHPBar, BorderLayout.CENTER);

        enemyFrame.add(enemyCenter, BorderLayout.CENTER);

        // RIGHT — HP LABEL
        enemyHPLabel = new JLabel("HP: --/--", SwingConstants.RIGHT);
        enemyHPLabel.setForeground(Color.WHITE);
        enemyHPLabel.setFont(GameFonts.pixelFont.deriveFont(20f));

        JPanel enemyRight = new JPanel(new BorderLayout());
        enemyRight.setBackground(Color.BLACK);
        enemyRight.add(enemyHPLabel, BorderLayout.EAST);

        enemyFrame.add(enemyRight, BorderLayout.EAST);

        enemyOuterFrame.add(enemyFrame, BorderLayout.CENTER);
        topContainer.add(enemyOuterFrame, BorderLayout.SOUTH);

        // CENTER — BATTLE LOG
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(0, 0, 0, 0)); // transparent
        battleLog.setForeground(Color.WHITE);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setFont(GameFonts.pixelFont.deriveFont(20f));
        battleLog.setOpaque(false); // ← important, ayaw tanduga ba

        logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Color.WHITE),
                new EmptyBorder(12, 12, 12, 12)));

        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false); // ← FIX
        logScroll.getViewport().setBackground(new Color(0, 0, 0, 0));

        logScroll.getVerticalScrollBar().setUI(new WhiteScrollBarUI());
        logScroll.getHorizontalScrollBar().setUI(new WhiteScrollBarUI());

        // BOTTOM — PLAYER FRAME + SKILLS
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.BLACK);
        bottomContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel playerFrame = new JPanel(new BorderLayout(10, 0));
        playerFrame.setBackground(Color.BLACK);
        playerFrame.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 2),
                new EmptyBorder(10, 12, 10, 12)));

        playerNameLabel = new JLabel("Player", SwingConstants.CENTER);
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(GameFonts.pixelFont.deriveFont(22f));
        playerNameLabel.setBorder(new EmptyBorder(0, 6, 0, 6));

        playerLevelLabel = new JLabel("Lv: 1", SwingConstants.LEFT);
        playerLevelLabel.setBorder(new EmptyBorder(0, 0, 0, 12));
        playerLevelLabel.setForeground(Color.WHITE);
        playerLevelLabel.setFont(GameFonts.pixelFont.deriveFont(18f));

        playerHPBar = new HPBar(1, 1);
        playerHPBar.setPreferredSize(new Dimension(300, 18));

        playerHPLabel = new JLabel("HP: --/--", SwingConstants.RIGHT);
        playerHPLabel.setBorder(new EmptyBorder(0, 12, 0, 6));
        playerHPLabel.setForeground(Color.WHITE);
        playerHPLabel.setFont(GameFonts.pixelFont.deriveFont(20f));

        JPanel playerCenter = new JPanel(new BorderLayout(6, 0));
        playerCenter.setBackground(Color.BLACK);
        playerCenter.add(playerHPBar, BorderLayout.CENTER);

        JPanel playerRight = new JPanel(new BorderLayout());
        playerRight.setBackground(Color.BLACK);
        playerRight.add(playerLevelLabel, BorderLayout.WEST);
        playerRight.add(playerHPLabel, BorderLayout.EAST);

        playerFrame.add(playerNameLabel, BorderLayout.WEST);
        playerFrame.add(playerCenter, BorderLayout.CENTER);
        playerFrame.add(playerRight, BorderLayout.EAST);

        bottomContainer.add(playerFrame, BorderLayout.NORTH);

        // BTNS
        JPanel bottomButtons = new JPanel(new GridBagLayout());
        bottomButtons.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 0, 18);

        Font btnFont = GameFonts.pixelFont.deriveFont(22f);

        skillBtn1 = new JButton("Skill 1");
        skillBtn2 = new JButton("Skill 2");
        skillBtn3 = new JButton("Skill 3");

        UIUtils.styleLargeButton(skillBtn1, btnFont);
        UIUtils.styleLargeButton(skillBtn2, btnFont);
        UIUtils.styleLargeButton(skillBtn3, btnFont);

        gbc.gridx = 0;
        bottomButtons.add(skillBtn1, gbc);
        gbc.gridx = 1;
        bottomButtons.add(skillBtn2, gbc);
        gbc.gridx = 2;
        bottomButtons.add(skillBtn3, gbc);

        bottomContainer.add(bottomButtons, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(logScroll, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);
    }

    // Game logic is delegated to BattleEngine.
    public void startBattle(Entity chosenPlayer) {
        clearListeners();

        skillBtn1.addActionListener(e -> playerUseSkill(0));
        skillBtn2.addActionListener(e -> playerUseSkill(1));
        skillBtn3.addActionListener(e -> playerUseSkill(2));

        engine.startBattle(chosenPlayer);
    }

    private void clearListeners() {
        for (ActionListener al : skillBtn1.getActionListeners())
            skillBtn1.removeActionListener(al);
        for (ActionListener al : skillBtn2.getActionListeners())
            skillBtn2.removeActionListener(al);
        for (ActionListener al : skillBtn3.getActionListeners())
            skillBtn3.removeActionListener(al);
    }

    private void playerUseSkill(int index) {
        if (!engine.isPlayerTurn()) {
            return;
        }

        engine.playerUseSkill(index);
        updateSkillButtons();

        Timer timer = new Timer(900, e -> {
            ((Timer) e.getSource()).stop();
            engine.enemyTurn();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void updateSkillButtons() {
        if (engine.getPlayer() == null) {
            return;
        }

        Skill[] skills = engine.getPlayer().getSkills();

        skillBtn1.setText(skills[0].getName()
                + (skills[0].isOnCooldown() ? " (CD: " + skills[0].getCurrentCooldown() + ")" : ""));
        skillBtn2.setText(skills[1].getName()
                + (skills[1].isOnCooldown() ? " (CD: " + skills[1].getCurrentCooldown() + ")" : ""));
        skillBtn3.setText(skills[2].getName()
                + (skills[2].isOnCooldown() ? " (CD: " + skills[2].getCurrentCooldown() + ")" : ""));
    }

    // Battle resolution is now handled by BattleEngine; BattlePanel only updates UI
    // and delegates actions.
    private void clearBattleLog() {
        battleLog.setText("");
    }

    // private void healBetweenBattles() {
    // int healAmount = player.getMaxHealth(); // changed from 60 to
    // player.getMaxHealth()
    // player.setCurrentHealth(Math.min(player.getMaxHealth(),
    // player.getCurrentHealth() + healAmount));
    // updateHPLabels();
    // log("- You have recovered your vitality for the next battle!");
    // }

    // private void updateHPLabels() {

    // if (player != null) {
    // playerHPLabel.setText("HP: " + player.getCurrentHealth() + "/" +
    // player.getMaxHealth());
    // playerHPBar.updateHP(player.getCurrentHealth(), player.getMaxHealth());
    // playerLevelLabel.setText("Lv: " + player.getLevel());
    // } else {
    // playerHPLabel.setText("HP: --/--");
    // playerLevelLabel.setText("LV: --/--");
    // }

    // if (enemy != null) {
    // enemyHPLabel.setText("HP: " + enemy.getCurrentHealth() + "/" +
    // enemy.getMaxHealth());
    // enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
    // } else {
    // enemyHPLabel.setText("HP: --/--");
    // }
    // }

    private void log(String msg) {
        battleLog.append(msg + "\n\n");
    }

    private void disableSkillButtons() {
        skillBtn1.setEnabled(false);
        skillBtn2.setEnabled(false);
        skillBtn3.setEnabled(false);
    }

    private void enableSkillButtons() {
        skillBtn1.setEnabled(true);
        skillBtn2.setEnabled(true);
        skillBtn3.setEnabled(true);
    }

    public JButton getBackButton() {
        return backButton;
    }

    private void createEndOverlay() {
        endOverlay = new JPanel(null);
        endOverlay.setBackground(new Color(0, 0, 0, 180));
        endOverlay.setVisible(false);

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.BLACK);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        box.setBounds(0, 0, 760, 220);
        box.setName("endOverlayBox");

        endText = new JLabel("", SwingConstants.CENTER);
        endText.setForeground(Color.WHITE);
        endText.setFont(GameFonts.pixelFont.deriveFont(32f));
        endText.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel prompt = new JLabel("Press any key or click to return to menu", SwingConstants.CENTER);
        prompt.setForeground(Color.WHITE);
        prompt.setFont(GameFonts.pixelFont.deriveFont(18f));
        prompt.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        box.add(endText, BorderLayout.CENTER);
        box.add(prompt, BorderLayout.SOUTH);

        endOverlay.add(box);

        endOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hideEndOverlayAndReturn();
            }
        });

        endOverlay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                hideEndOverlayAndReturn();
            }
        });

        endOverlay.setFocusable(true);
    }

    private void showEndOverlay(String message) {
        if (glass != null)
            glass.setVisible(true);
        if (endOverlay != null) {
            endText.setText("<html><div style='text-align:center'>" + message.replace("\n", "<br>") + "</div></html>");
            endOverlay.setBounds(0, 0, getWidth(), getHeight());
            endOverlay.setVisible(true);

            Component popup = null;
            for (Component c : endOverlay.getComponents()) {
                if ("endOverlayBox".equals(c.getName())) {
                    popup = c;
                    break;
                }
            }

            if (popup != null) {
                popup.setLocation((endOverlay.getWidth() - popup.getWidth()) / 2,
                        (endOverlay.getHeight() - popup.getHeight()) / 2);
            }

            endOverlay.requestFocusInWindow();
        }
    }

    private void hideEndOverlayAndReturn() {
        if (endOverlay != null)
            endOverlay.setVisible(false);
        if (storyOverlay != null)
            storyOverlay.setVisible(false);
        if (tutorialOverlay != null)
            tutorialOverlay.setVisible(false);
        if (exitOverlay != null)
            exitOverlay.setVisible(false);
        if (glass != null)
            glass.setVisible(false);

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame instanceof GameFrame) {
            ((GameFrame) frame).showMenu();
        }
    }

    private void createExitOverlay() {

        exitOverlay = new JPanel();
        exitOverlay.setLayout(null);
        exitOverlay.setBackground(new Color(0, 0, 0, 180)); // dark semi-transparent
        exitOverlay.setVisible(false);

        // container
        JPanel box = new JPanel(null);
        box.setBackground(Color.BLACK);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        box.setBounds(0, 0, 320, 180);

        box.setName("exitWindow");
        exitOverlay.add(box);

        JLabel prompt = new JLabel("Exit to Main Menu?", SwingConstants.CENTER);
        prompt.setForeground(Color.WHITE);
        prompt.setFont(GameFonts.pixelFont.deriveFont(20f));
        prompt.setBounds(10, 20, 300, 30);
        box.add(prompt);

        JButton yes = new JButton("Yes");
        yes.setFont(GameFonts.pixelFont.deriveFont(18f));
        yes.setFocusable(false);
        yes.setForeground(Color.WHITE);
        yes.setBackground(Color.BLACK);
        yes.setBounds(40, 90, 100, 40);
        yes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.add(yes);

        JButton no = new JButton("No");
        no.setFont(GameFonts.pixelFont.deriveFont(18f));
        no.setFocusable(false);
        no.setForeground(Color.WHITE);
        no.setBackground(Color.BLACK);
        no.setBounds(180, 90, 100, 40);
        no.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.add(no);

        yes.addActionListener(e -> {
            exitOverlay.setVisible(false);
            if (glass != null)
                glass.setVisible(false);

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof GameFrame) {
                ((GameFrame) frame).showMenu();
            }
        });

        no.addActionListener(e -> {
            exitOverlay.setVisible(false);
            if (glass != null)
                glass.setVisible(false);
        });
    }

    private void showExitOverlay() {
        if (glass != null) {
            glass.setVisible(true);
        }

        exitOverlay.setBounds(0, 0, getWidth(), getHeight());
        exitOverlay.setVisible(true);

        Component popup = null;
        for (Component c : exitOverlay.getComponents()) {
            if ("exitWindow".equals(c.getName())) {
                popup = c;
                break;
            }
        }

        if (popup != null) {
            int px = (exitOverlay.getWidth() - popup.getWidth()) / 2;
            int py = (exitOverlay.getHeight() - popup.getHeight()) / 2;
            popup.setLocation(Math.max(0, px), Math.max(0, py));

            if (glass != null) {
                glass.revalidate();
                glass.repaint();
            }
        }
    }

    private void createStoryOverlay() {
        storyOverlay = new JPanel(null);
        storyOverlay.setBackground(new Color(0, 0, 0, 180)); // dark semi-transparent
        storyOverlay.setVisible(false);

        JPanel box = new JPanel(null);
        box.setBackground(Color.BLACK);
        box.setName("storyBox");
        box.setBounds(0, 0, 520, 300);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        box.setLayout(new BorderLayout());
        storyOverlay.add(box);

        storyText = new JLabel("", SwingConstants.CENTER);
        storyText.setForeground(Color.WHITE);
        storyText.setFont(GameFonts.pixelFont.deriveFont(32f));
        storyText.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        box.add(storyText, BorderLayout.CENTER);

        storyContinue = new JLabel("PRESS ANY KEY TO CONTINUE >>", SwingConstants.RIGHT);
        storyContinue.setOpaque(true);
        storyContinue.setForeground(Color.WHITE);
        storyContinue.setBackground(Color.BLACK);
        storyContinue.setFont(GameFonts.pixelFont.deriveFont(24f));
        storyContinue.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        box.add(storyContinue, BorderLayout.SOUTH);

        storyOverlay.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                showNextStoryMessage();
            }

        });

        storyOverlay.setFocusable(true);
        storyOverlay.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                showNextStoryMessage();
            }

        });
    }

    public void showStoryOverlay(String text) {
        storyQueue.enqueue(text);

        if (!storyActive) {
            showNextStoryMessage();
        }
    }

    // Sakit sa mata ug ulo bruh
    // ===============================================//
    private void showNextStoryMessage() {
        if (isStoryTyping) {
            if (storyTypeTimer != null) {
                storyTypeTimer.stop();
            }
            storyText.setText(
                    "<html><div style='text-align:center'>" + storyFullText.replace("\n", "<br>") + "</div></html>");
            storyContinue.setVisible(true);
            isStoryTyping = false;
            return;
        }

        if (storyQueue.isEmpty()) {
            storyActive = false;
            hideStoryOverlay();
            return;
        }

        storyActive = true;
        String nextText = storyQueue.dequeue();

        if (glass != null)
            glass.setVisible(true);

        // Typewriter effect for story
        storyText.setText("");
        storyContinue.setVisible(false);
        typeText(nextText);

        storyOverlay.setBounds(0, 0, getWidth(), getHeight());
        storyOverlay.setVisible(true);
        storyOverlay.requestFocusInWindow();

        for (Component c : storyOverlay.getComponents()) {
            if ("storyBox".equals(c.getName())) {
                int px = (storyOverlay.getWidth() - c.getWidth()) / 2;
                int py = (storyOverlay.getHeight() - c.getHeight()) / 2;
                c.setLocation(px, py);
            }
        }

        glass.revalidate();
        glass.repaint();
    }

    // ===============================================//
    private void hideStoryOverlay() {
        storyOverlay.setVisible(false);
        if (glass != null)
            glass.setVisible(false);
    }

    // ===============================================//
    private void createTutorialOverlay() {
        tutorialOverlay = new JPanel(null);
        tutorialOverlay.setBackground(new Color(0, 0, 0, 180));
        tutorialOverlay.setVisible(false);

        JPanel box = new JPanel(null);
        box.setBackground(Color.BLACK);
        box.setName("tutorialBox");
        box.setBounds(0, 0, 900, 560); // MUCH LARGER
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        box.setLayout(new BorderLayout());
        tutorialOverlay.add(box);

        tutorialText = new JLabel("", SwingConstants.CENTER);
        tutorialText.setForeground(Color.WHITE);
        tutorialText.setFont(GameFonts.pixelFont.deriveFont(20f));
        tutorialText.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        box.add(tutorialText, BorderLayout.CENTER);

        tutorialContinue = new JLabel("PRESS ANY KEY TO CONTINUE >>", SwingConstants.RIGHT);
        tutorialContinue.setOpaque(true);
        tutorialContinue.setForeground(Color.WHITE);
        tutorialContinue.setBackground(Color.BLACK);
        tutorialContinue.setFont(GameFonts.pixelFont.deriveFont(23f));
        tutorialContinue.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        box.add(tutorialContinue, BorderLayout.SOUTH);

        // input
        tutorialOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNextTutorialMessage();
            }
        });
        tutorialOverlay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                showNextTutorialMessage();
            }
        });
        tutorialOverlay.setFocusable(true);
    }

    public void showTutorial(String text) {
        tutorialQueue.enqueue(text);

        if (!tutorialActive) {
            showNextTutorialMessage();
        }
    }

    private void showNextTutorialMessage() {
        if (tutorialQueue.isEmpty()) {
            tutorialActive = false;
            tutorialOverlay.setVisible(false);
            if (glass != null)
                glass.setVisible(false);
            return;
        }

        tutorialActive = true;

        String nextText = tutorialQueue.dequeue();

        if (glass != null)
            glass.setVisible(true);

        tutorialText.setText("<html><div style='text-align:center'>" +
                nextText.replace("\n", "<br>") +
                "</div></html>");

        tutorialOverlay.setBounds(0, 0, getWidth(), getHeight());
        tutorialOverlay.setVisible(true);
        tutorialOverlay.requestFocusInWindow();

        for (Component c : tutorialOverlay.getComponents()) {
            if ("tutorialBox".equals(c.getName())) {
                int px = (tutorialOverlay.getWidth() - c.getWidth()) / 2;
                int py = (tutorialOverlay.getHeight() - c.getHeight()) / 2;
                c.setLocation(px, py);
            }
        }

        glass.revalidate();
        glass.repaint();
    }

    @Override
    public void doLayout() {
        super.doLayout();

        if (exitOverlay != null) {
            exitOverlay.setBounds(0, 0, getWidth(), getHeight());

            Component popup = null;
            for (Component c : exitOverlay.getComponents()) {
                if ("exitWindow".equals(c.getName())) {
                    popup = c;
                    break;
                }
            }

            if (popup != null) {
                popup.setLocation(
                        (exitOverlay.getWidth() - popup.getWidth()) / 2,
                        (exitOverlay.getHeight() - popup.getHeight()) / 2);
            }
        }

        if (storyOverlay != null) {
            storyOverlay.setBounds(0, 0, getWidth(), getHeight());

            for (Component c : storyOverlay.getComponents()) {
                if ("storyBox".equals(c.getName())) {
                    c.setLocation(
                            (storyOverlay.getWidth() - c.getWidth()) / 2,
                            (storyOverlay.getHeight() - c.getHeight()) / 2);
                }
            }
        }

        if (tutorialOverlay != null) {
            tutorialOverlay.setBounds(0, 0, getWidth(), getHeight());
        }

        if (endOverlay != null) {
            endOverlay.setBounds(0, 0, getWidth(), getHeight());
            for (Component c : endOverlay.getComponents()) {
                if ("endOverlayBox".equals(c.getName())) {
                    c.setLocation(
                            (endOverlay.getWidth() - c.getWidth()) / 2,
                            (endOverlay.getHeight() - c.getHeight()) / 2);
                }
            }
        }

        for (Component c : tutorialOverlay.getComponents()) {
            if ("tutorialBox".equals(c.getName())) {
                c.setLocation(
                        (tutorialOverlay.getWidth() - c.getWidth()) / 2,
                        (tutorialOverlay.getHeight() - c.getHeight()) / 2);
            }
        }
    }

    // ===============================================//
    @Override
    public void addNotify() {
        super.addNotify();

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null && glass == null) {
            glass = (JPanel) frame.getGlassPane();
            glass.setLayout(null);
            glass.setOpaque(false);

            createExitOverlay();
            createStoryOverlay();
            createTutorialOverlay();
            createEndOverlay();

            // battle UI becomes unclickable
            glass.addMouseListener(new MouseAdapter() {
            });
            glass.addMouseMotionListener(new MouseMotionAdapter() {
            });

            if (glass != null && exitOverlay != null && exitOverlay.getParent() != glass) {
                glass.add(exitOverlay);
                exitOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            if (glass != null && tutorialOverlay != null && tutorialOverlay.getParent() != glass) {
                glass.add(tutorialOverlay);
                tutorialOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            if (glass != null && storyOverlay != null && storyOverlay.getParent() != glass) {
                glass.add(storyOverlay);
                storyOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            if (glass != null && endOverlay != null && endOverlay.getParent() != glass) {
                glass.add(endOverlay);
                endOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            for (Component c : storyOverlay.getComponents()) {
                if ("storyBox".equals(c.getName())) {
                    int px = (storyOverlay.getWidth() - c.getWidth()) / 2;
                    int py = (storyOverlay.getHeight() - c.getHeight()) / 2;
                    c.setLocation(px, py);
                }
            }
        }
    }

    // Effects parts to be transed later

    // With how StoryCont() is structed storyText bounces up and down. Until i find
    // a fix this remains commented :(
    // public void startBlinking() {
    // Timer t = new Timer(500, e -> {
    // storyContinue.setVisible(!storyContinue.isVisible());
    // });
    // t.start();
    // }

    // Typewriter eff, still need minor fixes in storyOverlay so no jumpy effs
    // TODO: Test setPreferredsize() on overlay, u never know ;)
    private void typeText(String fullText) {
        if (storyTypeTimer != null && storyTypeTimer.isRunning()) {
            storyTypeTimer.stop();
        }

        storyFullText = fullText;
        isStoryTyping = true;

        String header = "<html><div style='text-align:center'>";
        String footer = "</div></html>";
        String inner = fullText.replace("\n", "<br>");

        final char[] chars = inner.toCharArray();
        final StringBuilder current = new StringBuilder();

        storyTypeTimer = new Timer(20, null);
        storyTypeTimer.addActionListener(e -> {
            if (current.length() < chars.length) {
                current.append(chars[current.length()]);
                storyText.setText(header + current + footer);
            } else {
                storyTypeTimer.stop();
                storyContinue.setVisible(true);
                isStoryTyping = false;
            }
        });
        storyTypeTimer.start();
    }

    private Image backgroundImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {

            // Orig image size
            int imgWidth = backgroundImage.getWidth(null);
            int imgHeight = backgroundImage.getHeight(null);

            // Aspect ratios
            double imgAspect = (double) imgWidth / imgHeight;
            double panelAspect = (double) getWidth() / getHeight();

            int drawWidth, drawHeight;

            if (panelAspect > imgAspect) {
                // basically if panel is wider, I-match ang height
                drawHeight = getHeight();
                drawWidth = (int) (drawHeight * imgAspect);
            } else {
                // Opposite of above
                drawWidth = getWidth();
                drawHeight = (int) (drawWidth / imgAspect);
            }

            // Center it
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
        }
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

}