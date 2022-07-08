package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.PotionMetaProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class LegacyPotionMetaProvider implements PotionMetaProvider {
    @Override
    public ItemStack createPotionItem(final Material initial, final int effectId) {
        final ItemStack potion = new ItemStack(initial, 1);
        potion.setDurability((short) effectId);
        return potion;
    }

    @Override
    public String getDescription() {
        return "Legacy 1.8 Potion Meta Provider";
    }
}
