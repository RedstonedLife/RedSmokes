package com.bss.inc.redsmokes.main.perm.impl;

import com.bss.inc.redsmokes.main.RedSmokes;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ModernVaultHandler extends AbstractVaultHandler {
    private final List<String> supportedPlugins = Arrays.asList("PermissionsEx", "LuckPerms");

    @Override
    protected boolean emulateWildcards() {
        return false;
    }
    
    @Override
    public boolean tryProvider(RedSmokes ess) {
        return super.canLoad() && supportedPlugins.contains(getEnabledPermsPlugin());
    }
}
