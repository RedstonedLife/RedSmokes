package com.bss.inc.redsmokes.main.utils.logging;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class BaseLoggerProvider extends LoggerProvider {
    private final Logger logger;

    public BaseLoggerProvider(final Plugin plugin, final Logger logger) {
        super(plugin);
        this.logger = logger;
    }

    
}
