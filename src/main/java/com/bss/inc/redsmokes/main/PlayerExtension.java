package com.bss.inc.redsmokes.main;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class PlayerExtension {
    protected Player base;

    public PlayerExtension(final Player base) {
        this.base = base;
    }

    public final Player getBase() {return base;}
    public final Player setBase(final Player base) {return this.base = base;}
    public Server getServer() {return }
}
