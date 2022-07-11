package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.main.config.RedSmokesUserConfiguration;
import com.bss.inc.redsmokes.main.config.holders.UserConfigHolder;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;

public abstract class UserData extends PlayerExtension implements IConf {
    protected final transient IRedSmokes redsmokes;
    private final RedSmokesUserConfiguration config;
    private UserConfigHolder holder;
    private BigDecimal money;

    protected UserData(final Player base, final com.bss.inc.redsmokes.api.IRedSmokes redsmokes) {
        super(base);
        this.redsmokes = redsmokes;
        final File folder = new File(redsmokes.getDataFolder(), "userdata");
        if(!folder.exists()) {folder.mkdirs();}
        String filename;
        try {filename = base.getUniqueId().toString();}
        catch (final Throwable ex) {
            redsmokes.getLogger().warning("Falling back to old username system for " + base.getName());
            filename = base.getName();
        }

        config = new RedSmokesUserConfiguration(base.getName(), base.getUniqueId(), new File(folder, filename + ".yml"));
        
    }
}
