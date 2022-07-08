package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.logging.BaseLoggerProvider;
import com.bss.inc.redsmokes.main.utils.logging.LoggerProvider;
import com.bss.inc.redsmokes.main.utils.nms.refl.ReflUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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
}
