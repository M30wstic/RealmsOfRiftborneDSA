package com.ror.model;

public class SkySerpent extends Entity {
    public SkySerpent() {
        super("Sky Serpent", 90, 90, 29, 3); // fast, wind damage
    }
    @Override
    public void attack(Entity target) {
        int dmg = 15 + (int)(Math.random() * 8);  
        // 15–29 inclusive

        System.out.println(getName() + " attacks you for " + dmg + " damage!");
        target.takeDamage(dmg);
    }
}
