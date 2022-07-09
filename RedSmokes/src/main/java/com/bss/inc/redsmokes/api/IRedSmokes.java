package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.IRedSmokesModule;
import com.bss.inc.redsmokes.main.User;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IRedSmokes extends Plugin {
    void reload();
    Map<String, IrsCommand> getCommandMap();
    List<String> onTabCompleteRedSmokes(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IRedSmokesModule module);
    boolean onCommandEssentials(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IRedSmokesModule module);
    @Deprecated
    User getUser(Object base);
    User getUser(UUID base);
    User getUser(String base);
    User getUser(Player base);
    User matchUser(Server server, User sourceUser, String searchTerm, Boolean getHidden, boolean )
}
