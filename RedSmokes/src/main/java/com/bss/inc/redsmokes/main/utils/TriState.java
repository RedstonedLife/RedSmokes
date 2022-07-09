package com.bss.inc.redsmokes.main.utils;

import org.bukkit.entity.Player;

/**
 * A state that can be either true, false or unset.
 *
 * @see com.bss.inc.redsmokes.main.perm.IPermissionsHandler#isPermissionSetExact(Player, String)
 */
public enum TriState {
    TRUE,
    FALSE,
    UNSET
}
