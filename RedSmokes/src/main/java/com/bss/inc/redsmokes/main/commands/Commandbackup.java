package com.bss.inc.redsmokes.main.commands;

import com.bss.inc.redsmokes.main.Backup;
import com.bss.inc.redsmokes.main.CommandSource;
import org.bukkit.Server;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Commandbackup extends RedSmokesCommand {
    public Commandbackup() {
        super("backup");
    }

    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        final Backup backup = redSmokes.getBackup();
        if (backup == null) {
            throw new Exception(tl("backupDisabled"));
        }
        final String command = redSmokes.getSettings().getBackupCommand();
        if (command == null || "".equals(command) || "save-all".equalsIgnoreCase(command)) {
            throw new Exception(tl("backupDisabled"));
        }
        backup.run();
        sender.sendMessage(tl("backupStarted"));
    }
}
