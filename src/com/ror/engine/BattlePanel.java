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

    public int lastDamageTakenByPlayer = 0;
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
        //createExitOverlay();
        setBackground(Color.BLACK);

        // TOP BAR (title + icons + enemy box)
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.BLACK);
        topContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        //LEFT GAME TITLE
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
                new EmptyBorder(10, 12, 10, 12)
        ));

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
        battleLog.setOpaque(false); // ← important

        logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, Color.WHITE),
            new EmptyBorder(12, 12, 12, 12)
        ));

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
            new EmptyBorder(10, 12, 10, 12)
        ));

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

        //BTNS
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

        gbc.gridx = 0; bottomButtons.add(skillBtn1, gbc);
        gbc.gridx = 1; bottomButtons.add(skillBtn2, gbc);
        gbc.gridx = 2; bottomButtons.add(skillBtn3, gbc);

        bottomContainer.add(bottomButtons, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(logScroll, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);
    }
    


    //Game logic - shouldve been labeled from start(driving me nuts)
    public void startBattle(Entity chosenPlayer) {
        this.player = chosenPlayer;
        this.enemy = new Goblin(); // tutorial starts here

        playerShieldActive = false;
        enemyBlinded = false;
        delayedDamageToEnemy = 0;
        lastDamageTakenByPlayer = 0;
        playerTurn = true;
        mode = "Tutorial";
        MusicController.play("/com/ror/model/Assets/sfx/Tutorial.ogg", true);
        
        //=============================================
        //PLAYER + ENEMY WITH HP LABEL UPDATE
        playerNameLabel.setText(player.getName());
        enemyNameLabel.setText(enemy.getName());

        playerHPBar.updateHP(player.getCurrentHealth(), player.getMaxHealth()); 
        enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
        updateHPLabels();
        //=============================================


        Skill[] skills = player.getSkills();
        for (Skill sk : skills) sk.resetCooldown();
        skillBtn1.setText(skills[0].getName());
        skillBtn2.setText(skills[1].getName());
        
        skillBtn3.setText(skills[2].getName());

        skillBtn1.setEnabled(true);
        skillBtn2.setEnabled(true);
        skillBtn3.setEnabled(true);

        clearListeners();

        skillBtn1.addActionListener(e -> playerUseSkill(0));
        skillBtn2.addActionListener(e -> playerUseSkill(1));
        skillBtn3.addActionListener(e -> playerUseSkill(2));

        battleLog.setText("");
        setBackgroundImage("/com/ror/model/Assets/Backgrounds/Tutorial.png");
        log("- The Battle Begins. It's " + player.getName() + " VS " + enemy.getName() + "!");

        showTutorial(
                    " WELCOME TO REALMS OF RIFTBORNE! \n\n" + 
                    "I see you have selected " + player.getName() + ". Here's a little let-you-know:\n\n" +
                    "[!] You are pitted against a succession of enemies. Defeat each one of them to get through the levels.\n\n" +
                    "[!] Defeating a miniboss will allow you to proceed to the next realm.\n\n" +
                    "[!] You restore all health after every battle.\n\n" +
                    "[!] Your skills are your main method of attack, and certain skills will go on cooldown for a set amount of turns.\n\n" +
                    "[!] The Back button on the bottom right is disabled until AFTER the Tutorial!\n\n" +
                    "Pick a skill to begin your turn!");
        
        log("\n- Choose a skill to begin your turn.");

        updateSkillButtons();
    }

    private void clearListeners() {
        for (ActionListener al : skillBtn1.getActionListeners()) skillBtn1.removeActionListener(al);
        for (ActionListener al : skillBtn2.getActionListeners()) skillBtn2.removeActionListener(al);
        for (ActionListener al : skillBtn3.getActionListeners()) skillBtn3.removeActionListener(al);
    }

    private void playerUseSkill(int index) {
        if (!playerTurn) return;
        Skill s = player.getSkills()[index];

        if (s.isOnCooldown()) {
            log("- " + s.getName() + " is on cooldown for " + s.getCurrentCooldown() + " more turns!");
            return;
        }

        log("- " + player.getName() + " uses " + s.getName() + "!");

        String type = s.getType();
        switch (type.toLowerCase()) {
            case "chrono":
                // Andrew's Timeblade: immediate damage + burn over time (no delayed hit)
                int immediate = s.getPower() + player.getAtk();
                enemy.takeDamage(immediate);
                // configure burn: tune these values as desired
                burnDamageToEnemy = Math.max(1, s.getPower() / 3);
                burnTurnsRemaining = 3; // DOT lasts 3 enemy turns
                log("- Timeblade strikes for " + immediate + " damage and applies a burn (" + burnDamageToEnemy + " x " + burnTurnsRemaining + " turns)!");
                updateHPLabels();
                break;
            case "shield":
                playerShieldTurns = 2; // lasts for 2 enemy attacks
                log("- Time Shield bends time around you! Incoming damage reduced by 50% for 2 turns!");
                break;
            case "dodge":
               // Flashey's WindWalk: dodge incoming attack completely
               playerDodgeActive = true;
               log("- WindWalk activated! You'll evade the next attack completely!");
               break;
            case "reverse":
                int lost = player.getMaxHealth() - player.getCurrentHealth();
                int heal = (int) Math.ceil(lost * 0.5); // 50% of lost HP
                if (heal <= 0) {
                    log("- Reverse Flow restores 0 HP (you are already at full health).");
                } else {
                    player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + heal));
                    log("- Reverse Flow restores " + heal + " HP (50% of lost HP)!");
                    updateHPLabels();
                }
                break;
            case "heal":
               // Feather Barrier / healing skill: heals 40% of lost HP
               int lostHP = player.getMaxHealth() - player.getCurrentHealth();
               int healAmount = (int) Math.ceil(lostHP * 0.4);
               if (healAmount <= 0) {
                   log("- " + s.getName() + " — you are already at full health!");
               } else {
                   player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + healAmount));
                   log("- " + s.getName() + " restores " + healAmount + " HP (40% of lost HP)!");
                   updateHPLabels();
               }
               break;
            case "blind":
                enemyBlinded = true;
                log("- " + s.getName() + " — " + enemy.getName() + " is blinded and will miss the next attack!");
                break;
            default:
                enemy.takeDamage(s.getPower() + player.getAtk());
                log("- " + enemy.getName() + " takes " + (s.getPower() + player.getAtk()) + " damage!");
                updateHPLabels();
                break;
        }


        if (s.getCooldown() > 0) {
            s.triggerCooldown();
        }           


        // Reduce cooldowns for other skills
        for (Skill skill : player.getSkills()) {
            if (skill != s) skill.reduceCooldown();
        }
        updateSkillButtons();
        playerTurn = false;

        Timer timer = new Timer(900, e -> {
            ((Timer) e.getSource()).stop();
            enemyTurn();
        });
        timer.setRepeats(false);
        timer.start();
        
    }

    private void updateSkillButtons() {
        Skill[] skills = player.getSkills();

        skillBtn1.setText(skills[0].getName() + (skills[0].isOnCooldown() ? " (CD: " + skills[0].getCurrentCooldown() + ")" : ""));
        skillBtn2.setText(skills[1].getName() + (skills[1].isOnCooldown() ? " (CD: " + skills[1].getCurrentCooldown() + ")" : ""));
        skillBtn3.setText(skills[2].getName() + (skills[2].isOnCooldown() ? " (CD: " + skills[2].getCurrentCooldown() + ")" : ""));
    }

    private void enemyTurn() {
    // enemy's burn damage at start of turn;
    if (!enemy.isAlive()) {
        handleEnemyDefeat(enemy);
        return;
    }

    // enemy's burn damage at start of turn;
    if (burnTurnsRemaining > 0 && enemy.isAlive()) {
        enemy.takeDamage(burnDamageToEnemy);
        burnTurnsRemaining--;
        log("- Burn deals " + burnDamageToEnemy + " damage to " + enemy.getName() + " (" + burnTurnsRemaining + " turns remaining).");
        updateHPLabels();
        if (!enemy.isAlive()) {
            handleEnemyDefeat(enemy);
            return;
        }
    }

    // Enemy’s turn
    if (enemyBlinded) {
       log("- " + enemy.getName() + " is blinded by Shadowveil and misses the attack!");
       enemyBlinded = false;
       lastDamageTakenByPlayer = 0;
   } else if (playerDodgeActive) {
       log("- You dodge " + enemy.getName() + "'s attack with WindWalk!");
       playerDodgeActive = false;
       lastDamageTakenByPlayer = 0;
   } else if (playerShieldTurns > 0) {

       int damage = Math.max(0, enemy.getAtk() - player.getDef());
       int reduced = damage / 2;
       player.setCurrentHealth(player.getCurrentHealth() - reduced);
       lastDamageTakenByPlayer = reduced;

       log("- Time Shield distorts impact! Damge reduced from" + damage + " to " + reduced + ".");

       playerShieldTurns--;
       updateHPLabels();

   } else {
       int damage = Math.max(0, enemy.getAtk() - player.getDef());
       player.setCurrentHealth(player.getCurrentHealth() - damage);
       lastDamageTakenByPlayer = damage;
       log("- " + enemy.getName() + " attacks! You take " + damage + " damage.");
       updateHPLabels();
   }

    // Chrono Slash delayed damage
    if (delayedDamageToEnemy > 0 && enemy.isAlive()) {
        log("- Chrono Slash triggers — " + delayedDamageToEnemy + " delayed damage!");
        enemy.takeDamage(delayedDamageToEnemy);
        delayedDamageToEnemy = 0;
        updateHPLabels();

        if (!enemy.isAlive()) {
            handleEnemyDefeat(enemy);
            return;
        }
    }

    // Cooldown reductions
    for (Skill skill : player.getSkills()) {
        skill.reduceCooldown();
        updateSkillButtons();
    }

    // End turn check
    if (!player.isAlive()) {
        log("- You were defeated...");
        disableSkillButtons();
        return;
    }

    // Player’s next turn
    playerTurn = true;
        log("- Your turn! Choose your next skill.");
    }
    private void clearBattleLog() {
        battleLog.setText("");
    }


   private void handleEnemyDefeat(Entity defeatedEnemy) {
    log("- You defeated the " + defeatedEnemy.getName() + "!");
    disableSkillButtons();

    Timer nextBattleTimer = new Timer(700, e -> {
        ((Timer) e.getSource()).stop();

        // TUTORIAL PHASE
        if (mode.equals("Tutorial")) {
            if (defeatedEnemy instanceof Goblin) {
                log("You have been blessed by the Rift's energy! 💪");
                player.levelUp(0.10, 0.10);
                showStoryOverlay(
                    "The Goblin collapses, dropping a strange sigil...\n" +
                    "From the shadows, a hooded Cultist steps forward.");
                    updateSkillButtons();

                enemy = new Cultist();
                clearBattleLog();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                updateSkillButtons();
                return;
            }

            if (defeatedEnemy instanceof Cultist) {
                Music.stop();
                showStoryOverlay(
                    "The Cultist's whisper fades: 'He... watches from the Rift...'\n\n" +
                    "A surge of energy pulls you through - the Realms shift.");

                mode = "Realm1";
                MusicController.play("/com/ror/model/Assets/sfx/AetheriaTheme.ogg", true);
                showStoryOverlay(
                    " REALM I: AETHERIA \n\n" +
                    "You awaken beneath stormy skies - Aetheria.\n" +
                    "Sky Serpents circle above, lightning dancing across their scales.");
                    updateSkillButtons();

                setBackgroundImage("/com/ror/model/Assets/Backgrounds/Aetheria.png");

                enemy = new SkySerpent();   
                updateSkillButtons();
                clearBattleLog();
                player.levelUp(0.10, 0.10);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- You recall the expeprience form your fight with tutorial and use it to grow stronger! 💪");
                log("- A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }
        }

        // REALM I: AETHERIA
        if (mode.equals("Realm1")) {
            if (defeatedEnemy instanceof SkySerpent) {
                showStoryOverlay(
                    "The Sky Serpent bursts into feathers and lightning.\n" +
                    "From the thunderclouds above descends General Zephra, Storm Mage of the Rift.");
                    updateSkillButtons();

                enemy = new GeneralZephra();
                clearBattleLog();                
                player.levelUp(0.15, 0.15);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- You leveled up!");
                log("- A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }

            if (defeatedEnemy instanceof GeneralZephra) {
                Music.stop();
                showStoryOverlay(
                    "Zephra's thunderbird screeches as lightning fades.\n" +
                    "A fiery rift tears open beneath you...");
                    updateSkillButtons();

                mode = "Realm2";
                MusicController.play("/com/ror/model/Assets/sfx/IgnaraTheme.ogg", true);
                setBackgroundImage("/com/ror/model/Assets/Backgrounds/Ignara.png");
                enemy = new MoltenImp();
                clearBattleLog();
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- Realm II: Ignara — molten chaos awaits!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }
        }

        // REALM II: IGNARA
        if (mode.equals("Realm2")) {
            if (defeatedEnemy instanceof MoltenImp) {
                player.levelUp(0.10, 0.10);
                log("- LEVEL UP!!!");

                showStoryOverlay(
                    "The last Molten Imp bursts into flame...\n" +
                    "From the magma rises General Vulkrag, the Infernal Commander!");
                    updateSkillButtons();

                enemy = new GeneralVulkrag();
                clearBattleLog();
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }

            if (defeatedEnemy instanceof GeneralVulkrag) {
                Music.stop();
                showStoryOverlay(
                    "Vulkrag's molten armor cracks apart.\n" +
                    "Darkness seeps in from the edges of reality...");
                    updateSkillButtons();

                mode = "Realm3";
                MusicController.play("/com/ror/model/Assets/sfx/NoxterraTheme.ogg", true);
                setBackgroundImage("/com/ror/model/Assets/Backgrounds/Noxterra.png");
                enemy = new ShadowCreeper();
                healBetweenBattles();
                player.levelUp(0.15, 0.15);
                enemyNameLabel.setText(enemy.getName());
                log("- You noticable feel stronger after defeating a general! 💪");
                log("- Realm III: Noxterra — the shadows hunger...");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }
        }

        // REALM III: NOXTERRA
        if (mode.equals("Realm3")) {
            if (defeatedEnemy instanceof ShadowCreeper) {
                MusicController.stop();
                showStoryOverlay(
                    "The Shadow Creeper dissolves into mist...\n" +
                    "A dark laughter echoes — the Rift Lord himself descends.");
                    
                    updateSkillButtons();

                enemy = new Vorthnar();
                clearBattleLog();
                player.levelUp(0.20, 0.20);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
                log("- You feel a surge of power course through you! ");
                log("- The final boss approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;

                Sound.playThen("/com/ror/model/Assets/sfx/laugh.wav", () -> {
                    MusicController.play("/com/ror/model/Assets/sfx/Vorthar.ogg", true);
                });
                return;
            }

            if (defeatedEnemy instanceof Vorthnar) {
                showStoryOverlay(
                    "Vorthnar collapses — time itself shatters, then reforms.\n\n" +
                    "🏆 CHAPTER III COMPLETE 🏆\nYou have conquered the Realms!");

                log("🎉 You defeated Lord Vorthnar! Chapter III complete!");
                disableSkillButtons();
            }
        }
    });
    nextBattleTimer.setRepeats(false);
    nextBattleTimer.start();
    }

    private void healBetweenBattles() {
        int healAmount = player.getMaxHealth(); // changed from 60 to player.getMaxHealth()
        player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + healAmount));
        updateHPLabels();
        log("- You have recovered your vitality for the next battle!");
    }

    private void updateHPLabels() {

        if(player != null) {
            playerHPLabel.setText("HP: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
            playerHPBar.updateHP(player.getCurrentHealth(), player.getMaxHealth());
            playerLevelLabel.setText("Lv: " + player.getLevel());
        } else {
            playerHPLabel.setText("HP: --/--");
            playerLevelLabel.setText("LV: --/--");
        }

        if (enemy != null) {
            enemyHPLabel.setText("HP: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
            enemyHPBar.updateHP(enemy.getCurrentHealth(), enemy.getMaxHealth());
        } else {
            enemyHPLabel.setText("HP: --/--");
        }     
    }

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
            if(glass != null) glass.setVisible(false);

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if(frame instanceof GameFrame) {
                ((GameFrame) frame).showMenu();
            }
        });

        no.addActionListener(e -> {
            exitOverlay.setVisible(false);
            if (glass != null) glass.setVisible(false);
        });
    }

    private void showExitOverlay() {
        if (glass != null) {
            glass.setVisible(true);
        }

        exitOverlay.setBounds(0, 0, getWidth(), getHeight());
        exitOverlay.setVisible(true);

        Component popup = null;
        for( Component c : exitOverlay.getComponents()) {
            if ("exitWindow".equals(c.getName())) {
                popup = c;
                break;
            }
        }

        if(popup != null) {
            int px = (exitOverlay.getWidth() - popup.getWidth()) / 2;
            int py = (exitOverlay.getHeight() - popup.getHeight()) / 2;
            popup.setLocation(Math.max(0, px), Math.max(0, py));
        

            if(glass != null) {
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

        if(!storyActive) {
            showNextStoryMessage();
        }        
    }

//Sakit sa mata ug ulo bruh
//===============================================//
        private void showNextStoryMessage() {
        if (storyQueue.isEmpty()) {
            storyActive = false;
            hideStoryOverlay();
            return;
        }

        storyActive = true;
        String nextText = storyQueue.dequeue();

        if (glass != null) glass.setVisible(true);

        //Typewriter Effect
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
//===============================================//
    private void hideStoryOverlay() {
        storyOverlay.setVisible(false);
        if(glass != null) glass.setVisible(false);
    }
//===============================================//
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
            if (glass != null) glass.setVisible(false);
            return;
        }

        tutorialActive = true;

        String nextText = tutorialQueue.dequeue();

        if (glass != null) glass.setVisible(true);

        tutorialText.setText("<html><div style='text-align:center'>" +
            nextText.replace("\n","<br>") +
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
                    (exitOverlay.getHeight() - popup.getHeight()) / 2
                );
            }
        }

        if (storyOverlay != null) {
            storyOverlay.setBounds(0, 0, getWidth(), getHeight());

            for (Component c : storyOverlay.getComponents()) {
                if ("storyBox".equals(c.getName())) {
                    c.setLocation(
                        (storyOverlay.getWidth() - c.getWidth()) / 2,
                        (storyOverlay.getHeight() - c.getHeight()) / 2
                    );
                }
            }
        }

        if(tutorialOverlay != null) {
            tutorialOverlay.setBounds(0, 0, getWidth(), getHeight());
        }

        for(Component c : tutorialOverlay.getComponents()) {
            if("tutorialBox".equals(c.getName())) {
                c.setLocation(
                (tutorialOverlay.getWidth() - c.getWidth()) / 2,
                (tutorialOverlay.getHeight() - c.getHeight()) / 2
                );
            }
        }
    }
//===============================================//
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

            //battle UI becomes unclickable
            glass.addMouseListener(new MouseAdapter() {});
            glass.addMouseMotionListener(new MouseMotionAdapter() {});
            
            if(glass != null && exitOverlay != null && exitOverlay.getParent() != glass){
                glass.add(exitOverlay);
                exitOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            if(glass != null && tutorialOverlay != null && tutorialOverlay.getParent() != glass) {
                glass.add(tutorialOverlay);
                tutorialOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            if(glass != null && storyOverlay != null && storyOverlay.getParent() != glass) {
                glass.add(storyOverlay);
                storyOverlay.setBounds(0, 0, getWidth(), getHeight());
                glass.revalidate();
                glass.repaint();
            }

            for(Component c : storyOverlay.getComponents()) {
                if("storyBox".equals(c.getName())) {
                    int px = (storyOverlay.getWidth() - c.getWidth()) / 2;
                    int py = (storyOverlay.getHeight() - c.getHeight()) / 2;
                    c.setLocation(px, py);
                }
            }
        }
    }


    //Effects parts to be transed later

    //With how StoryCont() is structed storyText bounces up and down. Until i find a fix this remains commented :(
    public void startBlinking() {
        Timer t = new Timer(500, e -> {
            storyContinue.setVisible(!storyContinue.isVisible());
        });
        t.start();
    }

    //Typewriter eff,  still need minor fixes in storyOverlay so no jumpy effs
    //TODO: Test setPreferredsize() on overlay, u never know ;)
    private void typeText(String fullText) {
        String header = "<html><div style='text-align:center'>";
        String footer = "</div></html>";
        String inner = fullText.replace("\n", "<br>");

        final char[] chars = inner.toCharArray();
        final StringBuilder current = new StringBuilder();

        Timer typeTimer = new Timer(20, null);
        typeTimer.addActionListener(e -> {
            if (current.length() < chars.length) {
                current.append(chars[current.length()]);
                // Only update the INNER text, not the wrapper
                storyText.setText(header + current + footer);
            } else {
                typeTimer.stop();
                storyContinue.setVisible(true);
            }
        });
        typeTimer.start();
    }



    private Image backgroundImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {

            // Orig image size
            int imgWidth  = backgroundImage.getWidth(null);
            int imgHeight = backgroundImage.getHeight(null);

            // Aspect ratios
            double imgAspect   = (double) imgWidth / imgHeight;
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