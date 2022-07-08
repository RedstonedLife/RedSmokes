package com.bss.inc.redsmokes.main;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSource {
    protected CommandSender sender;

    public CommandSource(final CommandSender base) {
        this.sender = base;
    }

    public final CommandSender getSender() {
        return sender;
    }

    public final Player getPlayer() {
        if(sender instanceof Player) {
            return (Player) sender;
        }
    }
}
