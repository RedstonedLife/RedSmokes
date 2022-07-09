package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Backup implements Runnable {
    private transient final Server server;
    private transient final IRedSmokes redSmokes;
    private final AtomicBoolean pendingShutdown = new AtomicBoolean(false);
    private transient boolean running = false;
    private transient int taskId = -1;
    private transient boolean active = false;
    private transient CompletableFuture<Object> taskLock = null;

    public Backup(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        server = redSmokes.getServer();
        if(!redSmokes.getOnlinePlayers().isEmpty() || redSmokes.getSettings().isAlwaysRunBackup()) {
            redSmokes.runTaskAsynchronously(this::startTask);
        }
    }

    public void onPlayerJoin() {
        startTask();
    }

    public synchronized void stopTask() {
        running = false;
        if (taskId != -1) {
            server.getScheduler().cancelTask(taskId);
        }
        taskId = -1;
    }

    private synchronized void startTask() {
        if(!running) {
            final long interval = redSmokes.getSettings().getBackupInterval() * 1200; // minutes -> ticks
            if(interval < 1200) {
                return;
            }
            taskId = redSmokes.scheduleSyncRepeatingTask(this, interval, interval);
            running = true;
        }
    }

    public CompletableFuture<Object> getTaskLock() {
        return taskLock;
    }

    public void setPendingShutdown(final boolean shutdown) {
        pendingShutdown.set(shutdown);
    }

    @Override
    public void run() {
        if(active) {
            return;
        }
        final String command = redSmokes.getSettings().getBackupCommand();
        if(command == null || "".equals(command)) {
            return;
        }
        active = true;
        taskLock = new CompletableFuture<>();
        if("save-all".equalsIgnoreCase(command)) {
            final CommandSender cs = server.getConsoleSender();
            server.dispatchCommand(cs, "save-all");
            active = false;
            taskLock.complete(new Object());
            return;
        }
        redSmokes.getLogger().log(Level.INFO, tl("backupStarted"));
        final CommandSender cs = server.getConsoleSender();
        server.dispatchCommand(cs, "save-all");
        server.dispatchCommand(cs, "save-off");

        redSmokes.runTaskAsynchronously(() -> {
            try {
                final ProcessBuilder childBuilder = new ProcessBuilder(command.split(" "));
                childBuilder.redirectErrorStream(true);
                childBuilder.directory(redSmokes.getDataFolder().getParentFile().getParentFile());
                final Process child = childBuilder.start();
                redSmokes.runTaskLaterAsynchronously(() -> {
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()))) {
                        String line;
                        do {
                            line = reader.readLine();
                            if(line != null) {
                                redSmokes.getLogger().log(Level.INFO, line);
                            }
                        } while (line != null);
                    }
                })
            }
        })
    }
}
