package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.SerializationProvider;
import org.bukkit.inventory.ItemStack;

public class PaperSerializationProvider implements SerializationProvider {

    @Override
    public byte[] serializeItem(ItemStack stack) {
        return stack.serializeAsBytes();
    }

    @Override
    public ItemStack deserializeItem(byte[] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }

    @Override
    public String getDescription() {
        return "Paper Serialization Provider";
    }
}
