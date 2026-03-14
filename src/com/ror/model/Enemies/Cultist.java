package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class Cultist extends Entity {
    public Cultist() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("Cultist of the Rift", 95, 95, 20, 4, 50, 50, 12, 0);
        
        setSkill(0, new Skill("Dark Bolt", 10, 1.1, "Attack", 2, false));
    }
}