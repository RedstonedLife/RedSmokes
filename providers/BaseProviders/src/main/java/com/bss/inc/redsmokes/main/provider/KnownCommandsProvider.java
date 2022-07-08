package com.bss.inc.redsmokes.main.provider;

import org.bukkit.command.Command;

import java.util.Map;

public interface KnownCommandsProvider extends Provider {
    Map<String, Command> getKnownCommands();
}
