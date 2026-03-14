package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class MoltenImp extends Entity {
    public MoltenImp() {
        super("Molten Imp", 80, 80, 25, 8, 40, 40, 18, 0);
        setSkill(0, new Skill("Ember Spit", 10, 1.1, "Attack", 2, false));
    }
}