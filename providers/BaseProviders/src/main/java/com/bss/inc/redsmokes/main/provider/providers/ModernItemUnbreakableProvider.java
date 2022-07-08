package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.ItemUnbreakableProvider;
import org.bukkit.inventory.meta.ItemMeta;

public class ModernItemUnbreakableProvider implements ItemUnbreakableProvider {
    @Override
    public void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
    }

    @Override
    public String getDescription() {
        return "1.11+ ItemMeta Unbreakable Provider";
    }
}
