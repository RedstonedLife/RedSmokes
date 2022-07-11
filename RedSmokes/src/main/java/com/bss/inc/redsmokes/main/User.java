package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.OfflinePlayer;
import com.bss.inc.redsmokes.api.MaxMoneyException;
import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.api.events.TransactionEvent;
import com.bss.inc.redsmokes.api.events.UserBalanceUpdateEvent;
import com.bss.inc.redsmokes.main.economy.EconomyLayer;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.utils.EnumUtil;
import com.bss.inc.redsmokes.main.utils.NumberUtil;
import com.bss.inc.redsmokes.main.utils.TriState;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import static com.bss.inc.redsmokes.main.I18n.tl;

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
        if (redsmokes.getSettings().isDebug()) {
            redsmokes.getLogger().log(Level.INFO, "checking if " + base.getName() + " has " + node + " (set-explicit) - " + result);
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
            return redsmokes.getPermissionsHandler().hasPermission(base, node);
        } catch (final Exception ex) {
            if (redsmokes.getSettings().isDebug()) {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    private boolean isPermSetCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return false;
        }

        try {
            return redsmokes.getPermissionsHandler().isPermissionSet(base, node);
        } catch (final Exception ex) {
            if (redsmokes.getSettings().isDebug()) {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    private TriState isAuthorizedExactCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return TriState.UNSET;
        }

        try {
            return redsmokes.getPermissionsHandler().isPermissionSetExact(base, node);
        } catch (final Exception ex) {
            if (redsmokes.getSettings().isDebug()) {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                redsmokes.getLogger().log(Level.SEVERE, "Permission System Error: " + redsmokes.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return TriState.UNSET;
        }
    }
    @Override
    public void giveMoney(final BigDecimal value) throws MaxMoneyException {
        giveMoney(value, null);
    }

    @Override
    public void giveMoney(final BigDecimal value, final CommandSource initiator) throws MaxMoneyException {
        giveMoney(value, initiator, UserBalanceUpdateEvent.Cause.UNKNOWN);
    }

    public void giveMoney(final BigDecimal value, final CommandSource initiator, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
        if (value.signum() == 0) {
            return;
        }
        setMoney(getMoney().add(value), cause);
        sendMessage(tl("addedToAccount", NumberUtil.displayCurrency(value, redsmokes)));
        if (initiator != null) {
            initiator.sendMessage(tl("addedToOthersAccount", NumberUtil.displayCurrency(value, redsmokes), this.getDisplayName(), NumberUtil.displayCurrency(getMoney(), redsmokes)));
        }
    }

    @Override
    public void payUser(final User reciever, final BigDecimal value) throws Exception {
        payUser(reciever, value, UserBalanceUpdateEvent.Cause.UNKNOWN);
    }

    public void payUser(final User reciever, final BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws Exception {
        if (value.compareTo(BigDecimal.ZERO) < 1) {
            throw new Exception(tl("payMustBePositive"));
        }

        if (canAfford(value)) {
            setMoney(getMoney().subtract(value), cause);
            reciever.setMoney(reciever.getMoney().add(value), cause);
            sendMessage(tl("moneySentTo", NumberUtil.displayCurrency(value, redsmokes), reciever.getDisplayName()));
            reciever.sendMessage(tl("moneyRecievedFrom", NumberUtil.displayCurrency(value, redsmokes), getDisplayName()));
            final TransactionEvent transactionEvent = new TransactionEvent(this.getSource(), reciever, value);
            redsmokes.getServer().getPluginManager().callEvent(transactionEvent);
        } else {
            throw new ChargeException(tl("notEnoughMoney", NumberUtil.displayCurrency(value, redsmokes)));
        }
    }

    @Override
    public void takeMoney(final BigDecimal value) {
        takeMoney(value, null);
    }

    @Override
    public void takeMoney(final BigDecimal value, final CommandSource initiator) {
        takeMoney(value, initiator, UserBalanceUpdateEvent.Cause.UNKNOWN);
    }

    public void takeMoney(final BigDecimal value, final CommandSource initiator, final UserBalanceUpdateEvent.Cause cause) {
        if (value.signum() == 0) {
            return;
        }
        try {
            setMoney(getMoney().subtract(value), cause);
        } catch (final MaxMoneyException ex) {
            redsmokes.getLogger().log(Level.WARNING, "Invalid call to takeMoney, total balance can't be more than the max-money limit.", ex);
        }
        sendMessage(tl("takenFromAccount", NumberUtil.displayCurrency(value, redsmokes)));
        if (initiator != null) {
            initiator.sendMessage(tl("takenFromOthersAccount", NumberUtil.displayCurrency(value, redsmokes), this.getDisplayName(), NumberUtil.displayCurrency(getMoney(), redsmokes)));
        }
    }

    @Override
    public boolean canAfford(final BigDecimal cost) {
        return canAfford(cost, true);
    }

    public boolean canAfford(final BigDecimal cost, final boolean permcheck) {
        if (cost.signum() <= 0) {
            return true;
        }
        final BigDecimal remainingBalance = getMoney().subtract(cost);
        if (!permcheck || isAuthorized("redsmokes.eco.loan")) {
            return remainingBalance.compareTo(redsmokes.getSettings().getMinMoney()) >= 0;
        }
        return remainingBalance.signum() >= 0;
    }

    public void dispose() {
        redsmokes.runTaskAsynchronously(this::_dispose);
    }

    private void _dispose() {
        if (!base.isOnline()) {
            this.base = new OfflinePlayer(getConfigUUID(), redsmokes.getServer());
        }
        cleanup();
    }

    public long getLastOnlineActivity() {
        return lastOnlineActivity;
    }

    public void setLastOnlineActivity(final long timestamp) {
        lastOnlineActivity = timestamp;
    }

    @Override
    public BigDecimal getMoney() {
        final long start = System.nanoTime();
        final BigDecimal value = _getMoney();
        final long elapsed = System.nanoTime() - start;
        if (elapsed > redsmokes.getSettings().getEconomyLagWarning()) {
            redsmokes.getLogger().log(Level.INFO, "Lag Notice - Slow Economy Response - Request took over {0}ms!", elapsed / 1000000.0);
        }
        return value;
    }

    @Override
    public void setMoney(final BigDecimal value) throws MaxMoneyException {
        setMoney(value, UserBalanceUpdateEvent.Cause.UNKNOWN);
    }

    private BigDecimal _getMoney() {
        if (redsmokes.getSettings().isEcoDisabled()) {
            if (redsmokes.getSettings().isDebug()) {
                redsmokes.getLogger().info("Internal economy functions disabled, aborting balance check.");
            }
            return BigDecimal.ZERO;
        }
        final EconomyLayer layer = EconomyLayers.getSelectedLayer();
        if (layer != null && (layer.hasAccount(getBase()) || layer.createPlayerAccount(getBase()))) {
            return layer.getBalance(getBase());
        }
        return super.getMoney();
    }

    public void setMoney(final BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
        if (redsmokes.getSettings().isEcoDisabled()) {
            if (redsmokes.getSettings().isDebug()) {
                redsmokes.getLogger().info("Internal economy functions disabled, aborting balance change.");
            }
            return;
        }
        final BigDecimal oldBalance = _getMoney();

        final UserBalanceUpdateEvent updateEvent = new UserBalanceUpdateEvent(this.getBase(), oldBalance, value, cause);
        redsmokes.getServer().getPluginManager().callEvent(updateEvent);
        final BigDecimal newBalance = updateEvent.getNewBalance();

        final EconomyLayer layer = EconomyLayers.getSelectedLayer();
        if (layer != null && (layer.hasAccount(getBase()) || layer.createPlayerAccount(getBase()))) {
            layer.set(getBase(), newBalance);
        }
        super.setMoney(newBalance, true);
        Trade.log("Update", "Set", "API", getName(), new Trade(newBalance, redsmokes), null, null, null, newBalance, redsmokes);
    }

    public void updateMoneyCache(final BigDecimal value) {
        if (redsmokes.getSettings().isEcoDisabled() || !EconomyLayers.isLayerSelected() || super.getMoney().equals(value)) {
            return;
        }
        try {
            super.setMoney(value, false);
        } catch (final MaxMoneyException ex) {
            // We don't want to throw any errors here, just updating a cache
        }
    }
    @Override
    public boolean isHidden() {
        return hidden;
    }
    @Override
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
        if (hidden) {
            setLastLogout(getLastOnlineActivity());
        }
    }
    public boolean isHidden(final Player player) {
        return hidden || !player.canSee(getBase());
    }
}
