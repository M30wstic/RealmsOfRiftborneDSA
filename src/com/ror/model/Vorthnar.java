package com.ror.model;

public class Vorthnar extends Entity {// the eternal overlord
    public Vorthnar() {
        super("Vorthnar, The Eternal", 400, 400, 65, 20); // time-based ultimate attacks
    }

    @Override
    public void attack(Entity target) {
        int dmg = 58 + (int)(Math.random() * 8);  
        // 58-65 inclusive

        System.out.println(getName() + " plunges you to the abyss, you take " + dmg + " damage!");
        target.takeDamage(dmg);
    }
}
