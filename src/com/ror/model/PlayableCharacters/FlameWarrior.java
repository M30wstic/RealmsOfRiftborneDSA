package com.ror.model.PlayableCharacters;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class FlameWarrior extends Entity {
    public FlameWarrior() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("Fehld", 140, 140, 32, 24, 70, 70, 14, 0);

        //Name, ManaCost, DmgMul, Type, CD, TargetSelf
        setSkill(0, new Skill("Chrono Slash", 20, 1.4, "Attack", 2, false));
        setSkill(1, new Skill("Time Shield", 18, 0.0, "Buff", 3, true));
        setSkill(2, new Skill("Reverse Flow", 25, 0.0, "Heal", 4, true));
    }
}