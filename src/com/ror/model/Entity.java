package com.ror.model;

public abstract class Entity {
    private String name;
    private int maxHealth;
    private int currHealth;
    private int attack;
    private int defense;
    private int maxMana;
    private int currentMana;
    private int speed;
    private int currentCooldown;
    private int level = 1;
    private Skill[] skills;
    
    //CONSTRUCTOR
    public Entity(String name, int maxHealth, int currHealth, int attack, int defense, int maxMana, int currMana, int speed, int currentCooldown) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.currHealth = currHealth;
        this.attack = attack;
        this.defense = defense;
        this.maxMana = maxMana;
        this.currentMana = currMana;
        this.speed = speed;
        this.currentCooldown = currentCooldown;
        this.skills = new Skill[3];
    }

    //GETTERS AND SETTERS
    //GETTERS
    public String getName() { return name; }

    public int getMaxHealth() { return maxHealth; }

    public int getCurrentHealth() { return currHealth; }

    public int getAttack() { return attack; }

    public int getDefense() { return defense; }

    public int getMaxMana() { return maxMana; }

    public int getCurrentMana() { return currentMana; }

    public int getSpeed() { return speed; }

    public int getCurrentCooldown() { return currentCooldown; }

    public int getLevel() { return level; }

    public Skill[] getSkills() { return skills; }

    ///SETTERS
    public void setCurrentHealth(int health) {
        this.currHealth = Math.max(0, Math.min(health, maxHealth));
    }

    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, Math.min(mana, maxMana));
    }

    public void setCurrentCooldown(int cooldown) {
        this.currentCooldown = cooldown;
    }

    public void setSkills(Skill[] skills) {
        this.skills = skills;
    }
    ///GETTER AND SETTER END
    


    ///STANDARD LOGIC STARTS HERE
    public void takeDamage(int dmg) {
        int actualDamage = Math.max(1, dmg - defense); //deal at least 1 damage
        this.currHealth = Math.max(0, this.currHealth - actualDamage);
        System.out.printf("%s took %d damage! (%d/%d HP left)%n", name, actualDamage, currHealth, maxHealth);
    }

    public void attack(Entity target) {
        System.out.printf("%s attacks %s for %d damage!%n", this.name, target.getName(), this.attack);
        target.takeDamage(this.attack);
    }

    public void useMana(int manaCost) {
        if (currentMana >= manaCost) {
            this.currentMana -= manaCost;
        } else {
            throw new IllegalStateException("Not enough mana!");
        }
    }
    
    public void restoreMana(int manaRestoreAmount) {
        this.currentMana = Math.min(maxMana, this.currentMana + manaRestoreAmount);
    }

    public boolean isAlive() {
        return this.currHealth > 0;
    }

    public void setSkill(int slot, Skill skill) {
        if (slot >= 0 && slot < skills.length) {
            skills[slot] = skill;
        } else {
            throw new IllegalArgumentException("Invalid skill slot: " + slot);
        }
    }

    public void useSkill(int slot, Entity target) {
            if (slot >= 0 && slot < skills.length && skills[slot] != null) {
                Skill skill = skills[slot];
                if (currentMana >= skill.getManaCost()) {
                    System.out.printf("%s uses %s on %s!%n", name, skill.getName(), target.getName());
                    useMana(skill.getManaCost());
                    skill.use(this, target);
                } else {
                    System.out.println(name + " doesn't have enough mana for " + skill.getName() + "!");
                }
            } else {
                System.out.println("No skill equipped in that slot!");
            }
        }

    public Skill getSkillByName(String name) {
        for (Skill skill : skills) {
            if (skill != null &&skill.getName().equalsIgnoreCase(name)) {
                return skill;
            }
        }
    return null;
    }

    public void levelUp(double hpPercent, double atkPercent) {
        int hpIncrease = (int) Math.round(maxHealth * hpPercent);
        int atkIncrease = (int) Math.round(attack * atkPercent);

        maxHealth += hpIncrease;
        currHealth = maxHealth; // Fully heal on level up
        attack += atkIncrease;
        
        if (skills != null) {
            for (Skill skill : skills) {
                if (skill != null) skill.resetCooldown();
            }
        }
        level++;
        System.out.printf("%s leveled up! Max HP +%d, ATK +%d%n", name, hpIncrease, atkIncrease);
    }

}