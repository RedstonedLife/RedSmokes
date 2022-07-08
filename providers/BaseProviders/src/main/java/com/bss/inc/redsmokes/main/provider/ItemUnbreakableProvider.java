package com.bss.inc.redsmokes.main.provider;

import org.bukkit.inventory.meta.ItemMeta;

public interface ItemUnbreakableProvider extends Provider {
    void setUnbreakable(ItemMeta meta, boolean unbreakable);
}
