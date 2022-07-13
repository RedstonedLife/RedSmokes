package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.economy.EconomyLayer;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import net.redsmokes.api.IConf;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.logging.Level;

public class RedSmokesPluginListener implements Listener, IConf {
    private final transient IRedSmokes redSmokes;

    public RedSmokesPluginListener(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(final PluginEnableEvent event) {
        redSmokes.getPermissionsHandler().setUseSuperperms(redSmokes.getSettings().useBukkitPermissions());
        redSmokes.getPermissionsHandler().checkPermissions();
        redSmokes.getAlternativeCommandsHandler().addPlugin(event.getPlugin());
        if (EconomyLayers.isServerStarted()) {
            final EconomyLayer layer = EconomyLayers.onPluginEnable(event.getPlugin());
            if (layer != null) {
                redSmokes.getLogger().log(Level.INFO, "RedSmokes found a compatible payment resolution method: " + layer.getName() + " (v" + layer.getPluginVersion() + ")!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(final PluginDisableEvent event) {
        redSmokes.getPermissionsHandler().checkPermissions();
        redSmokes.getAlternativeCommandsHandler().removePlugin(event.getPlugin());
        if (EconomyLayers.onPluginDisable(event.getPlugin())) {
            final EconomyLayer layer = EconomyLayers.getSelectedLayer();
            if (layer != null) {
                redSmokes.getLogger().log(Level.INFO, "RedSmokes found a new compatible payment resolution method: " + layer.getName() + " (v" + layer.getPluginVersion() + ")!");
            } else {
                redSmokes.getLogger().log(Level.INFO, "Active payment resolution method has been disabled! Falling back to Essentials' default payment resolution system!");
            }
        }
    }

    @Override
    public void reloadConfig() {
    }
}
