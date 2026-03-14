package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class ShadowCreeper extends Entity {

    public ShadowCreeper() {
        super("Shadow Creeper", 70, 70, 30, 6, 50, 50, 24, 0);
        setSkill(0, new Skill("Abyssal Claw", 12, 1.3, "Attack", 2, false));
    }
}