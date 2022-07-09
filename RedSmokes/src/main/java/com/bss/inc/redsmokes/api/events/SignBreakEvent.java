package com.bss.inc.redsmokes.api.events;

import com.bss.inc.redsmokes.api.IUser;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;
import org.bukkit.event.HandlerList;

/**
 * Fired when an RedSmokes sign is broken.
 *
 * This is primarily intended for use with EssentialsX's sign abstraction - external plugins should not listen on this event.
 */
public class SignBreakEvent extends SignEvent {
    private static final HandlerList handlers = new HandlerList();

    public SignBreakEvent(final RedSmokesSign.ISign sign, final RedSmokesSign essSign, final IUser user) {
        super(sign, essSign, user);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
