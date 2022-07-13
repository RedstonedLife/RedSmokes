package net.redsmokes.api;

import com.bss.inc.redsmokes.main.Worth;
import com.bss.inc.redsmokes.main.items.CustomItemResolver;
import com.bss.inc.redsmokes.main.provider.PotionMetaProvider;
import com.bss.inc.redsmokes.main.provider.SpawnEggProvider;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public interface IRedSmokes extends com.bss.inc.redsmokes.main.IRedSmokes {


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
     * you probably want to implement your own {@link IItemDb.ItemResolver}.</b>
     *
     * @return The custom item resolver
     */
    CustomItemResolver getCustomItemResolver();
}