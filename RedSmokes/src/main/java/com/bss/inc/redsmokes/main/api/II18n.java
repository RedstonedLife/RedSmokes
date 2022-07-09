package com.bss.inc.redsmokes.main.api;

import org.bukkit.entity.Player;

/**
 * Provides access to the current locale in use.
 *
 * @deprecated External plugins should prefer to use either the player's client language ({@link Player#getLocale()} or
 *             {@link com.bss.inc.redsmokes.api.II18n } in case of future additions.
 */
public interface II18n {
}
