package com.bss.inc.redsmokes.main.perm.impl;

import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.main.RedSmokes;
import com.bss.inc.redsmokes.main.utils.TriState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ConfigPermissionsHandler extends SuperpermsHandler {
    private final transient IRedSmokes redSmokes;

    public ConfigPermissionsHandler(final Plugin redSmokes) {
        this.redSmokes = (IRedSmokes) redSmokes;
    }

    @Override
    public boolean hasPermission(final Player base, final String node) {
        final String[] cmds = node.split("\\.", 2);
        return ess.getSettings().isPlayerCommand(cmds[cmds.length - 1]) || super.hasPermission(base, node);
    }

    @Override
    public TriState isPermissionSetExact(Player base, String node) {
        final String[] cmds = node.split("\\.", 2);
        return ess.getSettings().isPlayerCommand(cmds[cmds.length - 1]) ? TriState.TRUE : super.isPermissionSetExact(base, node);
    }

    @Override
    public String getBackendName() {
        return "RedSmokes";
    }

    @Override
    public boolean tryProvider(RedSmokes ess) {
        return true;
    }
}
