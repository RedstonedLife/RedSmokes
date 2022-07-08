package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class RedSmokes extends JavaPlugin implements IRedSmokes {

    Logger

    @Override
    public void onEnable() {
        super.onEnable();

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public <T> RegisteredServiceProvider<T> getServiceProvider(Class<T> clazz) {
        return Bukkit.getServicesManager().getRegistration(clazz);
    }
}
