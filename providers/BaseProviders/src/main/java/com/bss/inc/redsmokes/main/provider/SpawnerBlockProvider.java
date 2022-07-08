package com.bss.inc.redsmokes.main.provider;

import org.bukkit.block.CreatureSpawner;

public interface SpawnerBlockProvider extends Provider {
    void setMaxSpawnDelay(CreatureSpawner spawner, int delay);

    void setMinSpawnDelay(CreatureSpawner spawner, int delay);
}
