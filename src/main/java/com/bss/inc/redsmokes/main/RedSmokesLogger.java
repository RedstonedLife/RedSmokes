package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.logging.LoggerProvider;
import com.bss.inc.redsmokes.main.utils.nms.refl.ReflUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

public class RedSmokesLogger {
    private final static Map<String, LoggerProvider> loggerProviders = new HashMap<>();
    private final static MethodHandle loggerFieldHandle;

    static {
        try {
            final Field loggerField = ReflUtil.getFieldCached(JavaPlugin.class, "logger");
            //noinspection ConstantConditions
            loggerFieldHandle = loggerField;
        }
    }
}
