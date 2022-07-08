package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.logging.BaseLoggerProvider;
import com.bss.inc.redsmokes.main.utils.logging.LoggerProvider;
import com.bss.inc.redsmokes.main.refl.ReflUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedSmokesLogger {
    private final static Map<String, LoggerProvider> loggerProviders = new HashMap<>();
    private final static MethodHandle loggerFieldHandle;

    static {
        try {
            final Field loggerField = ReflUtil.getFieldCached(JavaPlugin.class, "logger");
            //noinspection ConstantConditions
            loggerFieldHandle = MethodHandles.lookup().unreflectSetter(loggerField);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get logger field handle", t);
        }
    }

    private RedSmokesLogger() {}

    public static LoggerProvider getLoggerProvider(final Plugin plugin) {
        if(loggerProviders.containsKey(plugin.getName())) {
            return loggerProviders.get(plugin.getName());
        }

        final Logger parentLogger = Logger.getLogger(plugin.getName());
        final LoggerProvider provider;
        /*
        @TODO Add support for PaperMC API Logger
         */
        provider = new BaseLoggerProvider(plugin, parentLogger);
        provider.setParent(parentLogger);
        loggerProviders.put(plugin.getName(), provider);
        return provider;
    }

    public static void updatePluginLogger(final Plugin plugin) {
        final LoggerProvider provider = getLoggerProvider(plugin);
        try {
            loggerFieldHandle.invoke(plugin, provider);
        } catch (Throwable e) {
            provider.log(Level.SEVERE, "Failed to update " + plugin.getName() + " logger", e);
        }
    }

    public static LoggerProvider getLoggerProvider(final String pluginName) {
        if(loggerProviders.containsKey(pluginName)) {
            return loggerProviders.get(pluginName);
        }

        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if(plugin==null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginName);
        }
        return getLoggerProvider(plugin);
    }
}
