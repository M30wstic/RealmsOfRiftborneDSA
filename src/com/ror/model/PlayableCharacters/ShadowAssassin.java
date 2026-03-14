package com.ror.model.PlayableCharacters;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class ShadowAssassin extends Entity {
    public ShadowAssassin() {
        super("Zyane", 85, 85, 34, 12, 90, 90, 30, 0);

        setSkill(0, new Skill("Shadow Blink", 20, 1.5, "Attack", 2, false));
        setSkill(1, new Skill("Night Poison", 18, 0.8, "Debuff", 3, false));
        setSkill(2, new Skill("Dark Veil", 16, 0.0, "Buff", 4, true));
    }
}