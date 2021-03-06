package com.bss.inc.redsmokes.main.provider;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnEggProvider extends Provider {
    ItemStack createEggItem(EntityType type) throws IllegalArgumentException;

    EntityType getSpawnedType(ItemStack eggItem) throws IllegalArgumentException;
}