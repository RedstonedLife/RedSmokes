package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.ItemUnbreakableProvider;
import org.bukkit.inventory.meta.ItemMeta;

public class LegacyItemUnbreakableProvider implements ItemUnbreakableProvider {
    @Override
    public void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        meta.spigot().setUnbreakable(unbreakable);
    }

    @Override
    public String getDescription() {
        return "Legacy ItemMeta Unbreakable Provider";
    }
}