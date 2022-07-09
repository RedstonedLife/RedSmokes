package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.*;
import com.bss.inc.redsmokes.main.commands.PlayerNotFoundException;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

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
    User matchUser(Server server, User sourceUser, String searchTerm, Boolean getHidden, boolean getOffline) throws PlayerNotFoundException;
    boolean canInteractWith(CommandSource interactor, User interactee);
    boolean canInteractWith(User interactor, User interactee);
    I18n getI18n();
    User getOfflineUser(String name);
    World getWorld(String name);
    int broadcastMessage(String message);
    int broadcastMessage(IUser sender, String message);
    int broadcastMessage(IUser sender, String message, Predicate<IUser> shouldExclude);
    int broadcastMessage(String permission, String message);
    ISettings getSettings();
    BukkitScheduler getScheduler();
    Backup getBackup();
    UpdateChecker getUpdateChecker();
}
