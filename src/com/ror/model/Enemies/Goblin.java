package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class Goblin extends Entity {
    public Goblin() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("Goblin", 75, 75, 12, 2, 0, 0, 10, 0);
        
        setSkill(0, new Skill("Dark Bolt", 10, 1.1, "Attack", 2, false));
    }
}