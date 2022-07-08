package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public interface IRedSmokes extends Plugin {
    void reload();
    Map<String, IrsCommand> getCommandMap();

}
