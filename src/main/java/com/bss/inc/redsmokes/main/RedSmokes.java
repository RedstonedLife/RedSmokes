package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
        LOGGER
    }
}
