package com.bss.inc.redsmokes.main.perm.impl;

import com.bss.inc.redsmokes.api.IRedSmokes;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ConfigPermissionsHandler extends SuperpermsHandler {
    private final transient IRedSmokes ess;

    public ConfigPermissionsHandler(final Plugin ess) {
        this.ess = (IRedSmokes) ess;
    }

    @Override
    public boolean canBuild(final Player base, final String group) {
        return true;
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
        return "Essentials";
    }

    @Override
    public boolean tryProvider(Essentials ess) {
        return true;
    }
}
