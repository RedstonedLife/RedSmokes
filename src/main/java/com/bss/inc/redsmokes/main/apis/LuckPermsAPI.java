package com.bss.inc.redsmokes.main.apis;

import com.bss.inc.redsmokes.api.apis.ILuckPerms;
import com.bss.inc.redsmokes.main.RedSmokes;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsAPI implements ILuckPerms {

    private LuckPerms luckPerms;

    public LuckPerms onStart() {
        if(RedSmokes.getProvidingPlugin(LuckPerms.class) != null) {
            luckPerms = RedSmokes.getProvidingPlugin(LuckPerms.class).
        }
    }

    public void onStop() {

    }

    public boolean isLoaded() {
        return false;
    }
}
