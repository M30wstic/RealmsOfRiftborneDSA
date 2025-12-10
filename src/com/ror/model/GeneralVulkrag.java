package com.ror.model;

public class GeneralVulkrag extends Entity {
    public GeneralVulkrag() {
        super("General Vulkrag", 220, 220, 45, 12); // wields giant flame axe
    }

    @Override
    public void attack(Entity target) {
        int dmg = 38 + (int)(Math.random() * 8);  
        // 38–45 inclusive

        System.out.println(getName() + " swings his flame axe for " + dmg + " damage!");
        target.takeDamage(dmg);
    }
}
