package com.ror.model.Enemies;

import com.ror.model.Entity;
import com.ror.model.Skill;

public class SkySerpent extends Entity {

    public SkySerpent() {
        super("Sky Serpent", 100, 100, 22, 10, 60, 60, 28, 0);
        setSkill(0, new Skill("Cyclone Bite", 15, 1.2, "Attack", 2, false));
    }
}