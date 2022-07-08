package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.KnownCommandsProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.util.Map;

public class PaperKnownCommandsProvider implements KnownCommandsProvider {
    @Override
    public Map<String, Command> getKnownCommands() {
        return Bukkit.getCommandMap().getKnownCommands();
    }

    @Override
    public String getDescription() {
        return "Paper Known Commands Provider";
    }
}
