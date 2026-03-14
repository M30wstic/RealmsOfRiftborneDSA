package com.ror.model.PlayableCharacters;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class SkyMage extends Entity {
    public SkyMage() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("Mleux", 90, 90, 22, 14, 120, 120, 26, 0);
        
        //Name, ManaCost, DmgMul, Type, CD, TargetSelf
        setSkill(0, new Skill("Tempest Gale", 24, 1.2, "Attack", 3, false));
        setSkill(1, new Skill("Feather Barrier", 22, 0.0, "Buff", 4, true));
        setSkill(2, new Skill("Windwalk", 15, 0.0, "Buff", 3, true));
    }
}