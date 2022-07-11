package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.main.config.RedSmokesUserConfiguration;
import com.bss.inc.redsmokes.main.config.holders.UserConfigHolder;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public abstract class UserData extends PlayerExtension implements IConf {
    protected final transient IRedSmokes redsmokes;
    private final RedSmokesUserConfiguration config;
    private UserConfigHolder holder;
    private BigDecimal money;

    protected UserData(final Player base, final IRedSmokes redsmokes) {
        
    }
}
