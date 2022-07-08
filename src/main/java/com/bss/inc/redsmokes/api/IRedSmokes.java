package com.bss.inc.redsmokes.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;

import java.util.Map;

public interface IRedSmokes {
    void reload();
    Map<String, IrsCommand> getCommandMap();
}
