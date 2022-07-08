package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RedSmokes extends JavaPlugin implements IRedSmokes {

    private static final Logger BUKKIT_LOGGER = Logger.getLogger("RedSmokes");
    private static Logger LOGGER = null;

    @Override
    public void onEnable() {
        try {
            if(BUKKIT_LOGGER != super.getLogger()) {
                BUKKIT_LOGGER.setParent(super.getLogger());
            }
            LOGGER = RedSmokesLogger.getLoggerProvider(this);
            RedSmokesLogger.updatePluginLogger(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public <T> RegisteredServiceProvider<T> getServiceProvider(Class<T> clazz) {
        return Bukkit.getServicesManager().getRegistration(clazz);
    }

    private void handleCrash(final Throwable exception) {
        final PluginManager pm = getServer().getPluginManager();
        LOGGER.log(Level.SEVERE, exception.toString());
        exception.printStackTrace();
        pm.registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOW)
            public void onPlayerJoin(final PlayerJoinEvent event) {
                event.getPlayer().sendMessage("Essentials failed to load, read the log file");
            }
        }, this);
        //for(final Player player : getOn)
    }
}