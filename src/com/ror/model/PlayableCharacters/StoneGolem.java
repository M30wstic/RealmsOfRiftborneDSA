package com.ror.model.PlayableCharacters;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class StoneGolem extends Entity {
    public StoneGolem() {
        super("Void", 170, 170, 24, 40, 60, 60, 10, 0);

        setSkill(0, new Skill("Earth Slam", 18, 1.1, "Attack", 3, false));
        setSkill(1, new Skill("Rockskin", 16, 0.5, "Buff", 3, true));
        setSkill(2, new Skill("Seismic Shock", 20, 1.0, "Attack", 4, false)); // AOE logic later
    }
}