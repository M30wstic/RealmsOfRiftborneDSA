package com.ror.gameengine;

import com.ror.gamemodel.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class BattlePanel extends JPanel {

    private static Font pixelFont;

    static {
        try {
            pixelFont = Font.createFont(
                Font.TRUETYPE_FONT, 
                BattlePanel.class.getResourceAsStream(
                    "/com/ror/gamemodel/assets/fonts/bytebounce.medium.ttf"
                    )
                ).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private GameFrame parent;
    private JButton backButton;
    private JTextArea battleLog;
    private JButton skillBtn1, skillBtn2, skillBtn3, backBtn;
    private JLabel playerHPLabel, enemyHPLabel, playerNameLabel, enemyNameLabel;
    private JLabel playerLevelLabel;
    private int healAmount;

    private Entity player;
    private Entity enemy;
    private boolean playerTurn = true;

    boolean playerShieldActive = false;
    boolean playerDodgeActive = false;
    private boolean enemyBlinded = false;
    private int delayedDamageToEnemy = 0;
    private int burnDamageToEnemy = 0;         
    private int burnTurnsRemaining = 0;        
    private int lastDamageTakenByPlayer = 0;
    private String mode = "Tutorial";
    private WorldManager worldManager = new WorldManager();

    private HPBar playerHPBar;
    private HPBar enemyHPBar;
    

    public BattlePanel(GameFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // TOP (enemy framed box and corner menu and save(soon) buttons)
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.BLACK);
        topContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        //Left game title
        JLabel titleLabel = new JLabel("Realms of Riftborne", SwingConstants.LEFT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(pixelFont.deriveFont(14f));
        topContainer.add(titleLabel, BorderLayout.WEST);

        //Right menu + save buttons placeholder
        JPanel cornerIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cornerIcons.setBackground(Color.BLACK);
        JButton saveIcon = new JButton("\uD83D\uDCBE"); // floppy icon placeholder
        JButton menuIcon = new JButton("\u2630"); //menu icon placeholder
        styleIconButton(saveIcon);
        styleIconButton(menuIcon);
        cornerIcons.add(saveIcon);
        cornerIcons.add(menuIcon);
        topContainer.add(cornerIcons, BorderLayout.EAST);

        //Enemy frame box in center
        JPanel enemyOuterFrame = new JPanel(new BorderLayout());
        enemyOuterFrame.setBackground(Color.BLACK);
        enemyOuterFrame.setBorder(new EmptyBorder(10, 200, 10, 200));

        JPanel enemyFrame = new JPanel(new BorderLayout(10, 0));
        enemyFrame.setBackground(Color.BLACK);
        enemyFrame.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.WHITE, 2),
            new EmptyBorder(8, 12, 8, 12)
        ));

        enemyNameLabel = new JLabel("Enemy", SwingConstants.CENTER);
        enemyNameLabel.setForeground(Color.WHITE);  
        enemyNameLabel.setFont(pixelFont.deriveFont(18f));
        enemyNameLabel.setBorder(new EmptyBorder(0, 6, 0, 6)); 
        
        enemyHPLabel = new JLabel("HP: --", SwingConstants.CENTER);
        enemyHPLabel.setForeground(Color.WHITE);
        enemyNameLabel.setFont(pixelFont.deriveFont(16f));
        enemyHPLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // initialize HP bars with dummy values — will be updated in startBattle()
        enemyHPBar = new HPBar(1, 1); // dummy init; updated in startBattle()
        enemyHPBar.setPreferredSize(new Dimension(300, 18));

        JPanel enemyCenter = new JPanel(new BorderLayout(6, 0));
        enemyCenter.setBackground(Color.BLACK);
        enemyCenter.add(enemyHPBar, BorderLayout.CENTER);

        enemyFrame.add(enemyNameLabel, BorderLayout.WEST);
        enemyFrame.add(enemyCenter, BorderLayout.CENTER);
        enemyFrame.add(enemyHPLabel, BorderLayout.EAST);

        enemyOuterFrame.add(enemyFrame, BorderLayout.CENTER);
        topContainer.add(enemyOuterFrame, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        
        // Center: battle log
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(Color.BLACK);
        battleLog.setForeground(Color.WHITE);
        battleLog.setFont(pixelFont.deriveFont(18f)); //under testing
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, Color.WHITE), 
            new EmptyBorder(12, 12, 12, 12)
        ));

        logScroll.setBackground(Color.BLACK);
        add(logScroll, BorderLayout.CENTER);

        // Bottom: player frame and skill buttons
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.BLACK);
        bottomContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        //PLayer framed stats
        JPanel playerFrame = new JPanel(new BorderLayout(10, 0));
        playerFrame.setBackground(Color.BLACK);
        playerFrame.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.WHITE, 2),
            new EmptyBorder(10, 12, 10, 12)
        ));

        playerNameLabel = new JLabel("Player", SwingConstants.CENTER);
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(pixelFont.deriveFont(18f));
        playerNameLabel.setBorder(new EmptyBorder(0, 6, 0, 6));

        playerLevelLabel = new JLabel("Lv: 1", SwingConstants.CENTER);
        playerLevelLabel.setForeground(Color.WHITE);
        playerLevelLabel.setFont(pixelFont.deriveFont(16f));
        playerLevelLabel.setHorizontalAlignment(SwingConstants.LEFT);

        playerHPBar = new HPBar(1, 1); // dummy init; updated in startBattle()
        playerHPBar.setPreferredSize(new Dimension(300, 18));

        playerHPLabel = new JLabel("HP: --", SwingConstants.CENTER);
        playerHPLabel.setForeground(Color.WHITE);
        playerHPLabel.setFont(pixelFont.deriveFont(16f));
        playerHPLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel playerCenter = new JPanel(new BorderLayout(6, 0));
        playerCenter.setBackground(Color.BLACK);
        playerCenter.add(playerHPBar, BorderLayout.CENTER);

        playerFrame.add(playerNameLabel, BorderLayout.WEST);
        playerFrame.add(playerCenter, BorderLayout.CENTER);

        JPanel playerRight = new JPanel(new BorderLayout());
        playerRight.setBackground(Color.BLACK);
        playerRight.add(playerLevelLabel, BorderLayout.WEST);
        playerRight.add(playerHPLabel, BorderLayout.EAST);
        playerRight.setBorder(new EmptyBorder(0, 8, 0, 8));

        playerFrame.add(playerRight, BorderLayout.EAST);

        bottomContainer.add(playerFrame, BorderLayout.NORTH);

        //Buttons
        JPanel bottomButtons = new JPanel();
        bottomButtons.setBackground(Color.BLACK);
        bottomButtons.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 0, 18);
        gbc.gridy = 0;


        Font btnFont = pixelFont.deriveFont(16f);
        skillBtn1 = new JButton("Skill 1");
        skillBtn2 = new JButton("Skill 2");
        skillBtn3 = new JButton("Skill 3");
        backBtn = new JButton("Back");

        // increase button font size
        styleLargeButton(skillBtn1, btnFont);
        styleLargeButton(skillBtn2, btnFont);
        styleLargeButton(skillBtn3, btnFont);
        styleLargeButton(backBtn, btnFont);

        //main skills centerd
        gbc.gridx = 0;
        bottomButtons.add(skillBtn1, gbc);
        gbc.gridx = 1;
        bottomButtons.add(skillBtn2, gbc);
        gbc.gridx = 2;
        bottomButtons.add(skillBtn3, gbc);

        //back button in lower right corn
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        bottomContainer.add(bottomButtons, BorderLayout.CENTER);
        bottomContainer.add(backPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);
        
        //Back btn behavior
        backBtn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to return to the Main Menu?",
            "Confirm Return",
            JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                parent.showMenu();
            }
        });
        backBtn.setEnabled(false);
    }
    
    //Helper funcs for icon styling
    private void styleIconButton(JButton b) {
        b.setFont(pixelFont.deriveFont(16f));
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(Color.WHITE, 2));
        b.setPreferredSize(new Dimension(36, 36));
    }

    private void styleLargeButton(JButton b, Font font) {
        b.setFont(font);
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.WHITE, 2),
            new EmptyBorder(12, 28, 12, 28)
        ));
        b.setPreferredSize(new Dimension(220, 64));
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
        
        //=============================================
        //PLAYER + ENEMY WITH HP LABEL UPDATE
        playerNameLabel.setText(player.getName());
        enemyNameLabel.setText(enemy.getName());

        playerHPBar = new HPBar(player.getCurrentHealth(), player.getMaxHealth());
        enemyHPBar = new HPBar(enemy.getCurrentHealth(), enemy.getMaxHealth());
        updateHPLabels(); 
        //=============================================


        Skill[] skills = player.getSkills();
        for (Skill sk : skills) sk.resetCooldown();
        skillBtn1.setText(skills[0].getName());
        skillBtn2.setText(skills[1].getName());
        skillBtn3.setText(skills[2].getName());

        clearListeners();

        skillBtn1.addActionListener(e -> playerUseSkill(0));
        skillBtn2.addActionListener(e -> playerUseSkill(1));
        skillBtn3.addActionListener(e -> playerUseSkill(2));

        battleLog.setText("");
        log("⚔️ The Battle Begins. It's " + player.getName() + " VS " + enemy.getName() + "!");

        showDialog(
                    "Welcome to Realms of Riftborne. I see you have selected " + player.getName() + ". Here's a little let-you-know:\n" +
                    "[] You are pitted against a succession of enemies. Defeat each one of them to get through the levels.\n" +
                    "[] Defeating a miniboss will allow you to proceed to the next realm.\n" +
                    "[] You restore " + healAmount + " health after every battle.\n" +
                    "[] Your skills are your main method of attack, and certain skills will go on cooldown for a set amount of turns.\n" +
                    "[!] You are pitted against a succession of enemies. Defeat each one of them to get through the levels.\n" +
                    "[!] Defeating a miniboss will allow you to proceed to the next realm.\n" +
                    "[!] You restore " + healAmount + " health after every battle.\n" +
                    "[!] Your skills are your main method of attack, and certain skills will go on cooldown for a set amount of turns.\n" +
                    "[!] The Back button on the bottom right is disabled until AFTER the Tutorial!\n" +
                    "Pick a skill to begin your turn!",
                    "Tutorial");
        
                    log("\nChoose a skill to begin your turn.");
    

        log("\nChoose a skill to begin your turn.");

        updateSkillButtons();
    }


    private void showDialog(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
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
            log("⏳ " + s.getName() + " is on cooldown for " + s.getCurrentCooldown() + " more turns!");
            return;
        }

        log(player.getName() + " uses " + s.getName() + "!");

        String type = s.getType();
        switch (type.toLowerCase()) {
            case "chrono":
                // Andrew's Timeblade: immediate damage + burn over time (no delayed hit)
                int immediate = s.getPower() + player.getAtk();
                enemy.takeDamage(immediate);
                // configure burn: tune these values as desired
                burnDamageToEnemy = Math.max(1, s.getPower() / 3);
                burnTurnsRemaining = 3; // DOT lasts 3 enemy turns
                log("⚔️ Timeblade strikes for " + immediate + " damage and applies a burn (" + burnDamageToEnemy + " x " + burnTurnsRemaining + " turns)!");
                updateHPLabels();
                break;
            case "shield":
                playerShieldActive = true;
                log("🛡️ Time Shield activated! You’ll block the next attack and get healed.");
                break;
            case "dodge":
               // Flashey's WindWalk: dodge incoming attack completely
               playerDodgeActive = true;
               log("💨 WindWalk activated! You'll evade the next attack completely!");
               break;
            case "reverse":
                int lost = player.getMaxHealth() - player.getCurrentHealth();
                int heal = (int) Math.ceil(lost * 0.5); // 50% of lost HP
                if (heal <= 0) {
                    log("♻️ Reverse F   low restores 0 HP (you are already at full health).");
                } else {
                    player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + heal));
                    log("♻️ Reverse Flow restores " + heal + " HP (50% of lost HP)!");
                    updateHPLabels();
                }
                break;
            case "heal":
               // Feather Barrier / healing skill: heals 40% of lost HP
               int lostHP = player.getMaxHealth() - player.getCurrentHealth();
               int healAmount = (int) Math.ceil(lostHP * 0.4);
               if (healAmount <= 0) {
                   log("✨ " + s.getName() + " — you are already at full health!");
               } else {
                   player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + healAmount));
                   log("✨ " + s.getName() + " restores " + healAmount + " HP (40% of lost HP)!");
                   updateHPLabels();
               }
               break;
            case "blind":
                enemyBlinded = true;
                log("🌑 " + s.getName() + " — " + enemy.getName() + " is blinded and will miss the next attack!");
                break;
            default:
                enemy.takeDamage(s.getPower() + player.getAtk());
                log("💥 " + enemy.getName() + " takes " + (s.getPower() + player.getAtk()) + " damage!");
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
        log("🔥 Burn deals " + burnDamageToEnemy + " damage to " + enemy.getName() + " (" + burnTurnsRemaining + " turns remaining).");
        updateHPLabels();
        if (!enemy.isAlive()) {
            handleEnemyDefeat(enemy);
            return;
        }
    }

    // Enemy’s turn
    if (enemyBlinded) {
       log("🌫️ " + enemy.getName() + " is blinded by Shadowveil and misses the attack!");
       enemyBlinded = false;
       lastDamageTakenByPlayer = 0;
   } else if (playerDodgeActive) {
       log("💨 You dodge " + enemy.getName() + "'s attack with WindWalk!");
       playerDodgeActive = false;
       lastDamageTakenByPlayer = 0;
   } else if (playerShieldActive) {
       log("🛡️ The attack is blocked by your Time Shield!");
       playerShieldActive = false;
       lastDamageTakenByPlayer = 0;
   } else {
       int damage = Math.max(0, enemy.getAtk() - player.getDef());
       player.setCurrentHealth(player.getCurrentHealth() - damage);
       lastDamageTakenByPlayer = damage;
       log("👹 " + enemy.getName() + " attacks! You take " + damage + " damage.");
       updateHPLabels();
   }

    // Chrono Slash delayed damage
    if (delayedDamageToEnemy > 0 && enemy.isAlive()) {
        log("💫 Chrono Slash triggers — " + delayedDamageToEnemy + " delayed damage!");
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
        log("💀 You were defeated...");
        disableSkillButtons();
        return;
    }

    // Player’s next turn
    playerTurn = true;
        log("Your turn! Choose your next skill.");
    }
    private void clearBattleLog() {
        battleLog.setText("");
    }


   private void handleEnemyDefeat(Entity defeatedEnemy) {
    log("🏆🏆🏆 You defeated the " + defeatedEnemy.getName() + "!");
    disableSkillButtons();

    Timer nextBattleTimer = new Timer(700, e -> {
        ((Timer) e.getSource()).stop();

        // TUTORIAL PHASE
        if (mode.equals("Tutorial")) {
            if (defeatedEnemy instanceof Goblin) {
                log("You have been blessed by the Rift's energy! 💪");
                player.levelUp(0.10, 0.10);
                showDialog(
                    "The Goblin collapses, dropping a strange sigil...\n" +
                    "From the shadows, a hooded Cultist steps forward.",
                    "Tutorial: Part II");
                    updateSkillButtons();

                enemy = new Cultist();
                clearBattleLog();
                enemyNameLabel.setText(enemy.getName());
                log("🔥 A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                updateSkillButtons();
                return;
            }

            if (defeatedEnemy instanceof Cultist) {
                showDialog(
                    "The Cultist's whisper fades: 'He... watches from the Rift...'\n\n" +
                    "A surge of energy pulls you through — the Realms shift.",
                    "End of Tutorial");

                mode = "Realm1";
                showDialog(
                    "🌩️ REALM I: AETHERIA 🌩️\n\n" +
                    "You awaken beneath stormy skies — Aetheria.\n" +
                    "Sky Serpents circle above, lightning dancing across their scales.",
                    "Chapter I: The Rift Opens");
                    updateSkillButtons();

                enemy = new SkySerpent();   
                updateSkillButtons();
                clearBattleLog();
                player.levelUp(0.10, 0.10);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                log("You recall the expeprience form your fight with tutorial and use it to grow stronger! 💪");
                log("⚔️ A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                enableBackButtonForRealDeal();
                return;
            }
        }

        // REALM I: AETHERIA
        if (mode.equals("Realm1")) {
            if (defeatedEnemy instanceof SkySerpent) {
                showDialog(
                    "The Sky Serpent bursts into feathers and lightning.\n" +
                    "From the thunderclouds above descends General Zephra, Storm Mage of the Rift.",
                    "⚡ Boss Battle: General Zephra ⚡");
                    updateSkillButtons();

                enemy = new GeneralZephra();
                clearBattleLog();                
                player.levelUp(0.15, 0.15);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                log("You leveled up!💪");
                log("⚡ A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }

            if (defeatedEnemy instanceof GeneralZephra) {
                showDialog(
                    "Zephra's thunderbird screeches as lightning fades.\n" +
                    "A fiery rift tears open beneath you...",
                    "🔥 Transition to Realm II: Ignara 🔥");
                    updateSkillButtons();

                mode = "Realm2";
                enemy = new MoltenImp();
                clearBattleLog();
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                log("🔥 Realm II: Ignara — molten chaos awaits!");
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
                log("LEVEL UP!!!");
                showDialog(
                    "The last Molten Imp bursts into flame...\n" +
                    "From the magma rises General Vulkrag, the Infernal Commander!",
                    "🔥 Boss Battle: General Vulkrag 🔥");
                    updateSkillButtons();

                enemy = new GeneralVulkrag();
                clearBattleLog();
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                log("🔥 A new foe approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }

            if (defeatedEnemy instanceof GeneralVulkrag) {
                showDialog(
                    "Vulkrag's molten armor cracks apart.\n" +
                    "Darkness seeps in from the edges of reality...",
                    "🌑 Transition to Realm III: Noxterra 🌑");
                    updateSkillButtons();

                mode = "Realm3";
                enemy = new ShadowCreeper();
                healBetweenBattles();
                player.levelUp(0.15, 0.15);
                enemyNameLabel.setText(enemy.getName());
                log("You noticable feel stronger after defeating a general! 💪");
                log("🌑 Realm III: Noxterra — the shadows hunger...");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }
        }

        // REALM III: NOXTERRA
        if (mode.equals("Realm3")) {
            if (defeatedEnemy instanceof ShadowCreeper) {
                showDialog(
                    "The Shadow Creeper dissolves into mist...\n" +
                    "A dark laughter echoes — the Rift Lord himself descends.",
                    "💀 Final Boss: Lord Vorthnar 💀");
                    updateSkillButtons();

                enemy = new Vorthnar();
                clearBattleLog();
                player.levelUp(0.20, 0.20);
                healBetweenBattles();
                enemyNameLabel.setText(enemy.getName());
                log("You feel a surge of power course through you! 💪");
                log("💀 The final boss approaches: " + enemy.getName() + "!");
                updateHPLabels();
                enableSkillButtons();
                playerTurn = true;
                return;
            }

            if (defeatedEnemy instanceof Vorthnar) {
                showDialog(
                    "Vorthnar collapses — time itself shatters, then reforms.\n\n" +
                    "🏆 CHAPTER III COMPLETE 🏆\nYou have conquered the Realms!",
                    "🎉 Victory!");

                log("🎉 You defeated Lord Vorthnar! Chapter III complete!");
                disableSkillButtons();
                return;
            }
        }
    });
    nextBattleTimer.setRepeats(false);
    nextBattleTimer.start();
}


    private void enableBackButtonForRealDeal() {
    backBtn.setEnabled(true);
    for (ActionListener al : backBtn.getActionListeners()) backBtn.removeActionListener(al);

    backBtn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Return to Main Menu? Your current progress will be lost.",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // 🔁 Change this depending on your main game structure
            // Example if using a card layout in GameFrame:
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame instanceof GameFrame) {
                ((GameFrame) topFrame).showMenu();
            }
        }
    });
}

    private void healBetweenBattles() {
        int healAmount = player.getMaxHealth(); // changed from 60 to player.getMaxHealth()
        player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + healAmount));
        updateHPLabels();
        log("💖 You have recovered your vitality for the next battle!");
    }

    private void updateHPLabels() {
        playerHPLabel.setText("HP: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
        enemyHPLabel.setText("HP: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
        updateLevelLabel(); // ensure level label stays current
    }

    private void updateLevelLabel() {
        if (player == null) {
            playerLevelLabel.setText("Level: --");
            return;
        }
        try {
            playerLevelLabel.setText("Level: " + player.getLevel());
        } catch (Exception ex) {
            // safe fallback if something goes wrong retrieving level
            playerLevelLabel.setText("Level: --");
            System.err.println("Warning: couldn't read player level: " + ex.getMessage());
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
}