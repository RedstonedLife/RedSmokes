package com.bss.inc.redsmokes.main.nms.refl.providers;

import com.bss.inc.redsmokes.main.nms.refl.SpawnEggRefl;
import com.bss.inc.redsmokes.main.provider.SpawnEggProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class ReflSpawnEggProvider implements SpawnEggProvider {

    @Override
    public ItemStack createEggItem(final EntityType type) throws IllegalArgumentException {
        try {
            return new SpawnEggRefl(type).toItemStack();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public EntityType getSpawnedType(final ItemStack eggItem) throws IllegalArgumentException {
        try {
            return SpawnEggRefl.fromItemStack(eggItem).getSpawnedType();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getDescription() {
        return "NMS Reflection Provider";
    }
}
