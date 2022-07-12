package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.main.items.CustomItemResolver;
import com.bss.inc.redsmokes.main.provider.PotionMetaProvider;
import com.bss.inc.redsmokes.main.provider.SpawnEggProvider;

import java.util.Collection;

public interface IRedSmokes extends com.bss.inc.redsmokes.main.IRedSmokes {
    /**
     * Get a list of players who are vanished.
     *
     * @return A list of players who are vanished
     */
    Collection<String> getVanishedPlayersNew();

    /**
     * Get the spawn egg provider for the current platform.
     *
     * @return The current active spawn egg provider
     */
    SpawnEggProvider getSpawnEggProvider();

    /**
     * Get the potion meta provider for the current platform.
     *
     * @return The current active potion meta provider
     */
    PotionMetaProvider getPotionMetaProvider();

    /**
     * Get the {@link CustomItemResolver} that is currently in use.
     *
     * <b>Note: external plugins should generally avoid using this. If you want to add custom items from your plugin,
     * you probably want to implement your own {@link com.bss.inc.redsmokes.api.IItemDb.ItemResolver}.</b>
     *
     * @return The custom item resolver
     */
    CustomItemResolver getCustomItemResolver();
}