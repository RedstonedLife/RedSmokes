package com.bss.inc.redsmokes.main.perm;

import org.bukkit.entity.Player;

import java.util.List;

public interface IPermissionsHandler {
    String getGroup(Player base);
    List<String> getGroups(Player base);
    boolean inGroup(Player base, String group);
    boolean hasPermission(Player base)
}
