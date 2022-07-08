package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.ServerStateProvider;
import org.bukkit.Bukkit;

public class PaperServerStateProvider implements ServerStateProvider {
    @Override
    public boolean isStopping() {
        return Bukkit.isStopping();
    }

    @Override
    public String getDescription() {
        return "Paper Server State Provider";
    }
}
