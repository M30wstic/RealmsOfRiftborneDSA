package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class GeneralZephra extends Entity {

    public GeneralZephra() {
        super("General Zephra", 220, 220, 30, 15, 150, 150, 32, 0);
        setSkill(0, new Skill("Skyward Rend", 30, 1.4, "Attack", 3, false));
        setSkill(1, new Skill("Tailwind", 15, 0.0, "Buff", 3, true));
    }
}