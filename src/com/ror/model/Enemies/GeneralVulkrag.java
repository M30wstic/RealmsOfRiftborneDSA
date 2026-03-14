package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class GeneralVulkrag extends Entity {
    public GeneralVulkrag() {
        // Name, MaxHP, CurrHP, Atk, Def, MaxMP, CurrMP, Spd, CD
        super("General Vulkrag", 250, 250, 38, 20, 100, 100, 12, 0);
        
        setSkill(0, new Skill("Magma Overdrive", 25, 1.5, "Attack", 3, false));
        setSkill(1, new Skill("Heat Shield", 20, 0.0, "Buff", 4, true));
    }
}