package com.ror.model;

public class Skill {
    private String name;
    private double damageMultiplier; // For scaling (e.g., 1.4 for 140% ATK)
    private int manaCost;             // Added to match Entity.java requirements
    private String type;              // Attack, Heal, Buff, Debuff
    private int cooldown;
    private int currentCooldown;
    private boolean targetSelf;       // Helps distinguish Buffs/Heals from Attacks

    public Skill(String name, int manaCost, double damageMultiplier, String type, int cooldown, boolean targetSelf) {
        this.name = name;
        this.manaCost = manaCost;
        this.damageMultiplier = damageMultiplier;
        this.type = type;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.targetSelf = targetSelf;
    }

    //GETTERS
    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public int getDamageMultiplier() { return (int) damageMultiplier; }
    public String getType() { return type; }
    public int getCooldown() { return cooldown; }
    public int getCurrentCooldown() { return currentCooldown; }
    public boolean isTargetSelf() { return targetSelf; }

    public void use(Entity user, Entity target) {
        if (isOnCooldown()) {
            System.out.println(name + " is on cooldown for " + currentCooldown + " more turns!");
            return;
        }

        //Attacks and Debuff-dealing skills
        if (type.equalsIgnoreCase("Attack") || type.equalsIgnoreCase("Debuff")) {
            int totalDamage = (int) (user.getAttack() * damageMultiplier);
            target.takeDamage(totalDamage);
        } 
        //Buffs and Heals
        else if (type.equalsIgnoreCase("Heal") || type.equalsIgnoreCase("Buff")) {
            // If type = heal, use damageMultiplier as a percentage of Max HP
            int healAmount = (int) (user.getMaxHealth() * damageMultiplier);
            user.setCurrentHealth(user.getCurrentHealth() + healAmount);
            System.out.printf("%s healed for %d HP!%n", user.getName(), healAmount);
        }

        triggerCooldown();
    }


    //COOLDOWN LOGIC
    public boolean isOnCooldown() {
        return currentCooldown > 0;
    }

    public void reduceCooldown() {
        if (currentCooldown > 0) currentCooldown--;
    }

    public void triggerCooldown() {
        this.currentCooldown = this.cooldown;
    }
    
    public void resetCooldown() {
        this.currentCooldown = 0;
    }
}