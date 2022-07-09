package com.bss.inc.redsmokes.api.events;

import com.bss.inc.redsmokes.api.IUser;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;
import org.bukkit.event.HandlerList;

/**
 * Fired when an RedSmokes sign is interacted with.
 *
 * This is primarily intended for use with RedSmokesX's sign abstraction - external plugins should not listen on this event.
 */
public class SignInteractEvent extends SignEvent {
    private static final HandlerList handlers = new HandlerList();

    public SignInteractEvent(final RedSmokesSign.ISign sign, final RedSmokesSign essSign, final IUser user) {
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
