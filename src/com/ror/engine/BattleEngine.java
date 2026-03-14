//BattleEngine is mainly responsible for managing the battle logic, turn order, and interactions between the player 
// and enemy entities. It communicates with the BattlePanel through a Listener interface to update the UI based on game events. 
// The engine handles skill usage, enemy actions, status effects, and progression through different realms and bosses in the game.

package com.ror.engine;

import com.ror.model.*;
import com.ror.model.Enemies.*;

public class BattleEngine {

    public interface Listener {
        void onLog(String message);
        void onUpdateHP(Entity player, Entity enemy);
        void onUpdateSkillButtons(Skill[] skills);
        void onShowStory(String text);
        void onShowTutorial(String text);
        void onClearBattleLog();
        void onSetBackground(String path);
        void onPlayMusic(String path, boolean loop);
        void onStopMusic();
        void onPlaySoundThen(String soundPath, Runnable nextAction);
        void onEnableSkillButtons(boolean enabled);
        void onPlayerTurnPrompt();
        void onGameOver();
        void onGameWin();
    }

    private Listener listener;
    private Entity player;
    private Entity enemy;

    private boolean playerTurn = true;
    private boolean playerDodgeActive = false;
    private boolean enemyBlinded = false;
    private int burnDamageToEnemy = 0;
    private int burnTurnsRemaining = 0;
    private int playerShieldTurns = 0;

    private String mode = "Tutorial";

    public BattleEngine(Listener listener) {
        this.listener = listener;
    }

    //GETTERS
    public boolean isPlayerTurn() { return playerTurn; }
    public Entity getPlayer() { return player; }
    public Entity getEnemy() { return enemy; }


    public void startBattle(Entity chosenPlayer) {
        this.player = chosenPlayer;
        this.enemy = new Goblin(); // Initiate tutorial enemy

        playerTurn = true;
        playerDodgeActive = false;
        enemyBlinded = false;
        burnDamageToEnemy = 0;
        burnTurnsRemaining = 0;
        playerShieldTurns = 0;
        mode = "Tutorial";

        listener.onPlayMusic("/com/ror/model/Assets/sfx/Tutorial.ogg", true);
        listener.onSetBackground("/com/ror/model/Assets/Backgrounds/Tutorial.png");
        listener.onClearBattleLog();

        listener.onLog("- The Battle Begins! " + player.getName() + " VS " + enemy.getName());
        listener.onUpdateHP(player, enemy);

        //Upon Starting Battle, all CDs reset
        for (Skill sk : player.getSkills()) {
            if (sk != null) sk.resetCooldown();
        }

        listener.onUpdateSkillButtons(player.getSkills());
        listener.onShowTutorial("WELCOME TO REALMS OF RIFTBORNE!\n\nPick a skill to begin!");
        listener.onPlayerTurnPrompt();
        listener.onEnableSkillButtons(true);
    }
    
    //DYNAMIC SKILL LOGIC POG
    public boolean playerUseSkill(int index) {
        if (!playerTurn || player == null || enemy == null) return false;

        Skill s = player.getSkills()[index];
        if (s == null) return false;

        if (s.isOnCooldown()) {
            listener.onLog("- " + s.getName() + " is on cooldown for " + s.getCurrentCooldown() + " turns!");
            return false;
        }

        listener.onLog("- " + player.getName() + " uses " + s.getName() + "!");

        String skillType = s.getType().toLowerCase();
        
        if (skillType.equals("chrono") || skillType.equals("attack")) {
            int damage = (int)(player.getAttack() * s.getDamageMultiplier());
            enemy.takeDamage(damage);
            
            if (skillType.equals("chrono")) {
                burnDamageToEnemy = Math.max(2, damage / 4);
                burnTurnsRemaining = 3;
                listener.onLog("- Chrono-burn applied to " + enemy.getName() + "!");
            }
        } 
        else if (skillType.equals("shield")) {
            playerShieldTurns = 2;
            listener.onLog("- Time Shield active! Damage reduced by 50%.");
        } 
        else if (skillType.equals("dodge")) {
            playerDodgeActive = true;
            listener.onLog("- Windwalk active! Next attack will be dodged.");
        } 
        else if (skillType.equals("heal") || skillType.equals("reverse")) {
            int lostHP = player.getMaxHealth() - player.getCurrentHealth();
            int healAmount = (int)(lostHP * 0.5); 
            player.setCurrentHealth(player.getCurrentHealth() + healAmount);
            listener.onLog("- Restored " + healAmount + " HP!");
        }

        s.triggerCooldown();

        //reduce cooldowns of other skills by 1 turn after using a skill
        for (Skill skill : player.getSkills()) {
            if (skill != null && skill != s) {
                skill.reduceCooldown();
            }
        }

        listener.onUpdateSkillButtons(player.getSkills());
        listener.onUpdateHP(player, enemy);
        playerTurn = false;
        return true;

    }

    public void enemyTurn() {
        if (enemy == null || player == null || !enemy.isAlive()) {
            if (enemy != null && !enemy.isAlive()) handleEnemyDefeat();
            return;
        }

        // Apply Burn effects firstly
        if (burnTurnsRemaining > 0) {
            enemy.takeDamage(burnDamageToEnemy);
            burnTurnsRemaining--;
            listener.onLog("- Burn deals " + burnDamageToEnemy + " to " + enemy.getName());
            if (!enemy.isAlive()) {
                handleEnemyDefeat();
                return;
            }
        }

        // Defensive checks
        if (playerDodgeActive) {
            listener.onLog("- " + player.getName() + " dodged the attack!");
            playerDodgeActive = false;
        } else {
            int rawDamage = Math.max(1, enemy.getAttack() - player.getDefense());
            if (playerShieldTurns > 0) {
                rawDamage /= 2;
                playerShieldTurns--;
                listener.onLog("- Shield reduced damage to " + rawDamage + "!");
            }
            player.setCurrentHealth(player.getCurrentHealth() - rawDamage);
            listener.onLog("- " + enemy.getName() + " deals " + rawDamage + " damage!");
        }

        listener.onUpdateHP(player, enemy);

        if (player.isAlive()) {
            playerTurn = true;
            listener.onEnableSkillButtons(true);
            listener.onPlayerTurnPrompt();
        } else {
            listener.onGameOver();
        }
    }

    private void handleEnemyDefeat() {
        listener.onLog("- " + enemy.getName() + " has been defeated!");
        
        //Realm Progression Logic (Linear, can be OOPd after Chito implements open world map)
        if (enemy instanceof Goblin) {
            player.levelUp(0.1, 0.1);
            enemy = new Cultist();
            listener.onLog("- A Cultist appears!");
        } 
        else if (enemy instanceof Cultist) {
            transitionToRealm("Realm1", new SkySerpent(), "AETHERIA", "/com/ror/model/Assets/Backgrounds/Aetheria.png");
        }
        else if (enemy instanceof SkySerpent) {
            player.levelUp(0.15, 0.15);
            enemy = new GeneralZephra();
            listener.onLog("- General Zephra descends!");
        }
        else if (enemy instanceof GeneralZephra) {
            transitionToRealm("Realm2", new MoltenImp(), "IGNARA", "/com/ror/model/Assets/Backgrounds/Ignara.png");
        }
        else if (enemy instanceof MoltenImp) {
            player.levelUp(0.1, 0.1);
            enemy = new GeneralVulkrag();
            listener.onLog("- General Vulkrag erupts from the magma!");
        }
        else if (enemy instanceof GeneralVulkrag) {
            transitionToRealm("Realm3", new ShadowCreeper(), "NOXTERRA", "/com/ror/model/Assets/Backgrounds/Noxterra.png");
        }
        else if (enemy instanceof ShadowCreeper) {
            player.levelUp(0.2, 0.2);
            enemy = new GumohNahn(); // renamed from Vorthnar
            listener.onLog("- Gumoh Nahn, The Eternal, has arrived.");
        }
        else if (enemy instanceof GumohNahn) {
            listener.onGameWin();
        }

        healBetweenBattles();
        listener.onUpdateHP(player, enemy);
        playerTurn = true;
        listener.onEnableSkillButtons(true);
    }

    private void transitionToRealm(String newMode, Entity firstEnemy, String realmName, String bgPath) {
        this.mode = newMode;
        this.enemy = firstEnemy;
        listener.onClearBattleLog();
        listener.onSetBackground(bgPath);
        listener.onShowStory("REALM: " + realmName);
        listener.onLog("- Welcome to " + realmName);
    }

    private void healBetweenBattles() {
        int healAmount = player.getMaxHealth();
        player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + healAmount));
        listener.onUpdateHP(player, enemy);
        listener.onLog("- You have recovered your vitality for the next battle!");
    }
}
