package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class ShadowWarlord extends Entity {
    
    public ShadowWarlord() {
        super("Shadow Warlord", 250, 250, 50, 14, 150, 150, 28, 0);
        setSkill(0, new Skill("Abyssal Slew", 12, 1.3, "Attack", 2, false));
        setSkill(1, new Skill("Dark Drain", 15, 0.0, "Buff", 3, true));
    }
}