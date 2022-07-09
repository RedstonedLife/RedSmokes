package com.bss.inc.redsmokes.api.events;

import com.bss.inc.redsmokes.api.IUser;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class SignEvent extends Event implements Cancellable {
    final RedSmokesSign.ISign sign;
    final RedSmokesSign redSign;
    final IUser user;
    private boolean cancelled = false;

    public SignEvent(final RedSmokesSign.ISign sign, final RedSmokesSign redSign, final IUser user) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.sign = sign;
        this.redSign = redSign;
        this.user = user;
    }

    public RedSmokesSign.ISign getSign() {
        return sign;
    }

    public RedSmokesSign getRedSign() {
        return redSign;
    }

    public IUser getUser() {
        return user;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
