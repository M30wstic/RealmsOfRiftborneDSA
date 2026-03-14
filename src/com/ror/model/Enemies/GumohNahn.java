package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class GumohNahn extends Entity {

    public GumohNahn() {
        // Phase 1 Stats
        super("Gumoh Nahn", 1000, 1000, 42, 30, 250, 250, 20, 0);
        
        setSkill(0, new Skill("Chrono Distortion", 40, 1.5, "Attack", 3, false));
        setSkill(1, new Skill("Time Loop", 50, 0.0, "Buff", 5, true));
        setSkill(2, new Skill("Shattered Timeline", 60, 1.8, "Attack", 6, false));
    }
}