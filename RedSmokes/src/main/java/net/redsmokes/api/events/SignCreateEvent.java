package net.redsmokes.api.events;

import net.redsmokes.api.IUser;
import com.bss.inc.redsmokes.main.textreader.signs.RedSmokesSign;
import org.bukkit.event.HandlerList;

public class SignCreateEvent extends SignEvent {
    private static final HandlerList handlers = new HandlerList();

    public SignCreateEvent(final RedSmokesSign.ISign sign, final RedSmokesSign redSign, final IUser user) {
        super(sign, redSign, user);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
