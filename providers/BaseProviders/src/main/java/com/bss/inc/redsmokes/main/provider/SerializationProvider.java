package com.bss.inc.redsmokes.main.provider;

import org.bukkit.inventory.ItemStack;

public interface SerializationProvider extends Provider {
    byte[] serializeItem(ItemStack stack);

    ItemStack deserializeItem(byte[] bytes);
}
