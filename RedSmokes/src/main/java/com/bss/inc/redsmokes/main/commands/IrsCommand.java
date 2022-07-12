package com.bss.inc.redsmokes.main.commands;

import net.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.main.CommandSource;
import com.bss.inc.redsmokes.main.IRedSmokesModule;
import com.bss.inc.redsmokes.main.User;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public interface IrsCommand {
    String getName();

    Map<String, String> getUsageStrings();

    void run(Server server, User user, String commandLabel, Command cmd, String[] args) throws Exception;

    void run(Server server, CommandSource sender, String commandLabel, Command cmd, String[] args) throws Exception;

    List<String> tabComplete(Server server, User user, String commandLabel, Command cmd, String[] args);

    List<String> tabComplete(Server server, CommandSource sender, String commandLabel, Command cmd, String[] args);

    void setEssentials(IRedSmokes ess);

    void setEssentialsModule(IRedSmokesModule module);

    void showError(CommandSender sender, Throwable throwable, String commandLabel);
}
