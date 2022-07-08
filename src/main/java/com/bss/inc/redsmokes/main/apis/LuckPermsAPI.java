package com.bss.inc.redsmokes.main.apis;

import com.bss.inc.redsmokes.api.apis.ILuckPerms;
import com.bss.inc.redsmokes.main.RedSmokes;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsAPI implements ILuckPerms {

    private LuckPerms luckPerms;
    private static final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

    public LuckPerms onStart() {
        if(provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    public void onStop() {

    }

    public boolean isLoaded() {
        return false;
    }
}
