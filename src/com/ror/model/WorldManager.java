package com.ror.model;

import com.ror.util.LinkedQueue;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class WorldManager {
    private final LinkedHashMap<String, LinkedQueue<Entity>> worlds = new LinkedHashMap<>();
    private Iterator<String> worldIterator;
    private String currentWorld;
    private boolean finalBossUnlocked = false;

    public WorldManager() {
        loadWorlds();
        worldIterator = worlds.keySet().iterator();

        // Ensure there's at least one world before calling next()
        if (worldIterator.hasNext()) {
            currentWorld = worldIterator.next(); // Start with first world
        } else {
            currentWorld = null;
        }
    }

    private void loadWorlds() {
        // 🌬️ Aetheria
        LinkedQueue<Entity> aetheria = new LinkedQueue<>();
        aetheria.enqueue(new SkySerpent());
        aetheria.enqueue(new SkySerpent());
        aetheria.enqueue(new GeneralZephra());
        worlds.put("Aetheria", aetheria);

        // 🔥 Ignara
        LinkedQueue<Entity> ignara = new LinkedQueue<>();
        ignara.enqueue(new MoltenImp());
        ignara.enqueue(new MoltenImp());
        ignara.enqueue(new GeneralVulkrag());
        worlds.put("Ignara", ignara);

        // 🌑 Noxterra
        LinkedQueue<Entity> noxterra = new LinkedQueue<>();
        noxterra.enqueue(new ShadowCreeper());
        noxterra.enqueue(new ShadowCreeper());
        noxterra.enqueue(new ShadowWarlord()); // corrected name
        worlds.put("Noxterra", noxterra);
    }

    public Entity getNextEnemy() {
        if (currentWorld == null) return null;

        LinkedQueue<Entity> enemies = worlds.get(currentWorld);

        // If the current world still has enemies
        if (enemies != null && !enemies.isEmpty()) {
            return enemies.dequeue();
        }

        // If current world cleared, move to the next
        if (worldIterator.hasNext()) {
            currentWorld = worldIterator.next();
            System.out.println("\n🌍 Entering " + currentWorld + "!");
            return getNextEnemy();
        }

        // All worlds cleared → spawn final boss
        if (!finalBossUnlocked) {
            finalBossUnlocked = true;
            currentWorld = "Final Battle";
            System.out.println("\n⚡ The Final Battle Begins! ⚡");
            return new Vorthnar();
        }

        // Game finished
        return null;
    }

    public String getCurrentWorld() {
        return currentWorld;
    }

    public boolean isFinalBossUnlocked() {
        return finalBossUnlocked;
    }
}
