package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.IRedSmokesModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public interface IRedSmokes extends Plugin {
    void reload();
    Map<String, IrsCommand> getCommandMap();

    List<String> onTabCompleteEssentials(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IRedSmokesModule module);

}
