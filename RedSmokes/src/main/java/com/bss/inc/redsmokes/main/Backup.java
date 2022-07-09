package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import org.bukkit.Server;

import java.util.concurrent.atomic.AtomicBoolean;

public class Backup implements Runnable {
    private transient final Server server;
    private transient final IRedSmokes redSmokes;
    private final AtomicBoolean pendingShutdown = new AtomicBoolean(false);
    private transient boolean running = false;
    
}
