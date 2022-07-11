package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.utils.EnumUtil;
import com.google.common.collect.Lists;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class User extends UserData implements com.bss.inc.redsmokes.api.IUser, Comparable<User> {
    private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");

    // User command confirmation strings
    private final Map<User, BigDecimal> confirmingPayments = new WeakHashMap<>();

    // User Properties
    private transient boolean vanished;
    private boolean hidden = false;

    // Misc
    private transient long lastOnlineActivity;
    private transient long lastThrottledAction;
    private transient long lastActivity = System.currentTimeMillis();
    private long lastNotifiedAboutMailsMs;
    private transient final List<String> signCopy = Lists.newArrayList("","","","");
    private transient long lastVanishTime = System.currentTimeMillis();

    public User(final Player base, final com.bss.inc.redsmokes.api.IRedSmokes redSmokes) {
        super(base, redSmokes);
        if(this.getBase().isOnline()) {
            lastOnlineActivity = System.currentTimeMillis();
        }
    }

    void update(final Player base) {setBase(base);}
    public IRedSmokes getRedSmokes() {return redsmokes;}
    @Override public boolean isAuthorized(final IrsCommand cmd) {return isAuthorized(cmd);}
    @Override
    public boolean isAuthorized(final IrsCommand cmd, final String permissionPrefix) {
        return isAuthorized(permissionPrefix + (cmd.getName().equals("r") ? "msg" : cmd.getName()));
    }
    @Override
    public boolean isAuthorized(final String node) {
        final boolean result = isAuthorizedCheck(node);
        if (redsmokes.getSettings().isDebug()) {
            redsmokes.getLogger().log(Level.INFO, "checking if " + base.getName() + " has " + node + " - " + result);
        }
        return result;
    }


}
