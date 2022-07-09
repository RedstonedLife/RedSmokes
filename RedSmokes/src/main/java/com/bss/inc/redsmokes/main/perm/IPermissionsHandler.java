package com.bss.inc.redsmokes.main.perm;

import org.bukkit.entity.Player;

import java.util.List;

public interface IPermissionsHandler {
    String getGroup(Player base);
    List<String> getGroups(Player base);
    boolean inGroup(Player base, String group);
    boolean hasPermission(Player base, String node);
    // Does not check for * permissions
    boolean isPermissionSet(Player base, String node);
    TriState isPermissionSetExact(Player base, String node);
    String getPrefix(Player base);
    String getSuffix(Player base);
    void registerContext(String context, Function<User, Iterable<String>>)
}
