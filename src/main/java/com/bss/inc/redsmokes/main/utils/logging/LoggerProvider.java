package com.bss.inc.redsmokes.main.utils.logging;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Level;

public abstract class LoggerProvider extends PluginLogger {
    public LoggerProvider(final Plugin plugin) {
        super(plugin);
    }

    protected abstract void doTheLog(Level level, String message, Throwable throwable);
    protected abstract void doTheLog(Level level, String message);
}
