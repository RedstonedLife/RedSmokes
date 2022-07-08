package com.bss.inc.redsmokes.main.utils.logging;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class PaperLoggerProvider extends LoggerProvider {
    private final ComponentLogger logger;

    public PaperLoggerProvider(Plugin plugin) {
        super(plugin);
        this.logger = plugin.getComponentLogger();
    }

    protected void doTheLog(Level level, String message, Throwable throwable) {
        TextComponent textComponent = LegacyComponentSerializer.legacySection().deserialize(message);
        if (level == Level.SEVERE) {
            this.logger.error((Component)textComponent, throwable);
        } else if (level == Level.WARNING) {
            this.logger.warn((Component)textComponent, throwable);
        } else if (level == Level.INFO) {
            this.logger.info((Component)textComponent, throwable);
        } else if (level == Level.FINE) {
            this.logger.trace((Component)textComponent, throwable);
        } else {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
    }

    protected void doTheLog(Level level, String message) {
        TextComponent textComponent = LegacyComponentSerializer.legacySection().deserialize(message);
        if (level == Level.SEVERE) {
            this.logger.error((Component)textComponent);
        } else if (level == Level.WARNING) {
            this.logger.warn((Component)textComponent);
        } else if (level == Level.INFO) {
            this.logger.info((Component)textComponent);
        } else if (level == Level.FINE) {
            this.logger.trace((Component)textComponent);
        } else {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
    }
}
