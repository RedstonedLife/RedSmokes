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
    @Override
    public boolean isPermissionSet(final String node) {
        final boolean result = isPermSetCheck(node);
        if (ess.getSettings().isDebug()) {
            ess.getLogger().log(Level.INFO, "checking if " + base.getName() + " has " + node + " (set-explicit) - " + result);
        }
        return result;
    }

    /**
     * Checks if the given permission is explicitly defined and returns its value, otherwise
     * {@link TriState#UNSET}.
     */
    public TriState isAuthorizedExact(final String node) {
        return isAuthorizedExactCheck(node);
    }

    private boolean isAuthorizedCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return false;
        }

        try {
            return ess.getPermissionsHandler().hasPermission(base, node);
        } catch (final Exception ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    private boolean isPermSetCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return false;
        }

        try {
            return ess.getPermissionsHandler().isPermissionSet(base, node);
        } catch (final Exception ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    private TriState isAuthorizedExactCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return TriState.UNSET;
        }

        try {
            return ess.getPermissionsHandler().isPermissionSetExact(base, node);
        } catch (final Exception ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return TriState.UNSET;
        }
    }

}
