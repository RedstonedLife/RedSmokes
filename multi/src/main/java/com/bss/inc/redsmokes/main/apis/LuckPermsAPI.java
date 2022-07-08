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
        if(provider != null) {luckPerms = provider.getProvider();return luckPerms;} else {return null;}
    }

    public void onStop() {luckPerms=null;}

    public boolean isLoaded() {return Bukkit.getPluginManager().isPluginEnabled("LuckPerms");}
}
