package com.ror.model.PlayableCharacters;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class TimeBlade extends Entity {
    public TimeBlade() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("Neo", 110, 110, 28, 18, 80, 80, 22, 0);

        //Name, ManaCost, DmgMul, Type, CD, TargetSelf
        setSkill(0, new Skill("Chrono Slash", 20, 1.4, "Attack", 2, false));
        setSkill(1, new Skill("Time Shield", 18, 0.0, "Buff", 3, true));
        setSkill(2, new Skill("Reverse Flow", 25, 0.0, "Heal", 4, true));
    }
}