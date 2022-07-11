package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.api.MaxMoneyException;
import com.bss.inc.redsmokes.main.config.ConfigurateUtil;
import com.bss.inc.redsmokes.main.config.RedSmokesUserConfiguration;
import com.bss.inc.redsmokes.main.config.entities.CommandCooldown;
import com.bss.inc.redsmokes.main.config.entities.LazyLocation;
import com.bss.inc.redsmokes.main.config.holders.UserConfigHolder;
import com.bss.inc.redsmokes.main.utils.NumberUtil;
import com.bss.inc.redsmokes.main.utils.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.bss.inc.redsmokes.main.I18n.tl;

public abstract class UserData extends PlayerExtension implements IConf {
    protected final transient IRedSmokes redsmokes;
    private final RedSmokesUserConfiguration config;
    private UserConfigHolder holder;
    private BigDecimal money;

    protected UserData(final Player base, final com.bss.inc.redsmokes.api.IRedSmokes redsmokes) {
        super(base);
        this.redsmokes = redsmokes;
        final File folder = new File(redsmokes.getDataFolder(), "userdata");
        if(!folder.exists()) {folder.mkdirs();}
        String filename;
        try {filename = base.getUniqueId().toString();}
        catch (final Throwable ex) {
            redsmokes.getLogger().warning("Falling back to old username system for " + base.getName());
            filename = base.getName();
        }

        config = new RedSmokesUserConfiguration(base.getName(), base.getUniqueId(), new File(folder, filename + ".yml"));
        reloadConfig();

        if(config.getUsername() == null) {
            config.setUsername(getLastAccountName());
        }
    }

    public final void reset() {
        config.blockingSave();
        config.getFile().delete();
        if (config.getUsername() != null) {
            ess.getUserMap().removeUser(config.getUsername());
            if (isNPC()) {
                final String uuid = UUID.nameUUIDFromBytes(("NPC:" + StringUtil.safeString(config.getUsername())).getBytes(Charsets.UTF_8)).toString();
                ess.getUserMap().removeUserUUID(uuid);
            }
        }
    }

    public final void cleanup() {
        config.blockingSave();
    }

    @Override
    public final void reloadConfig() {
        config.load();
        try {
            holder = config.getRootNode().get(UserConfigHolder.class);
        } catch (SerializationException e) {
            ess.getLogger().log(Level.SEVERE, "Error while reading user config: " + config.getFile().getName(), e);
            throw new RuntimeException(e);
        }
        config.setSaveHook(() -> {
            try {
                config.getRootNode().set(UserConfigHolder.class, holder);
            } catch (SerializationException e) {
                ess.getLogger().log(Level.SEVERE, "Error while saving user config: " + config.getFile().getName(), e);
                throw new RuntimeException(e);
            }
        });
        money = _getMoney();
    }

    private BigDecimal _getMoney() {
        BigDecimal result = ess.getSettings().getStartingBalance();
        final BigDecimal maxMoney = ess.getSettings().getMaxMoney();
        final BigDecimal minMoney = ess.getSettings().getMinMoney();

        // NPC banks are not actual player banks, as such they do not have player starting balance.
        if (isNPC()) {
            result = BigDecimal.ZERO;
        }

        if (holder.money() != null) {
            result = holder.money();
        }
        if (result.compareTo(maxMoney) > 0) {
            result = maxMoney;
        }
        if (result.compareTo(minMoney) < 0) {
            result = minMoney;
        }
        holder.money(result);

        return holder.money();
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(final BigDecimal value, final boolean throwError) throws MaxMoneyException {
        final BigDecimal maxMoney = redsmokes.getSettings().getMaxMoney();
        final BigDecimal minMoney = redsmokes.getSettings().getMinMoney();
        if (value.compareTo(maxMoney) > 0) {
            if (throwError) {
                throw new MaxMoneyException();
            }
            money = maxMoney;
        } else {
            money = value;
        }
        if (money.compareTo(minMoney) < 0) {
            money = minMoney;
        }
        holder.money(money);
        stopTransaction();
    }

    private String getHomeName(String search) {
        if (NumberUtil.isInt(search)) {
            try {
                search = getHomes().get(Integer.parseInt(search) - 1);
            } catch (final NumberFormatException | IndexOutOfBoundsException ignored) {
            }
        }
        return search;
    }

    public Location getHome(final String name) {
        final String search = getHomeName(name);
        final LazyLocation loc = holder.homes().get(search);
        return loc != null ? loc.location() : null;
    }

    public boolean hasValidHomes() {
        for (final LazyLocation loc : holder.homes().values()) {
            if (loc != null && loc.location() != null) {
                return true;
            }
        }
        return false;
    }

    public Location getHome(final Location world) {
        if (getHomes().isEmpty()) {
            return null;
        }
        for (final String home : getHomes()) {
            final Location loc = holder.homes().get(home).location();
            if (loc != null && world.getWorld() == loc.getWorld()) {
                return loc;
            }

        }
        return holder.homes().get(getHomes().get(0)).location();
    }

    public List<String> getHomes() {
        return new ArrayList<>(holder.homes().keySet());
    }

    public void setHome(String name, final Location loc) {
        //Invalid names will corrupt the yaml
        name = StringUtil.safeString(name);
        holder.homes().put(name, LazyLocation.fromLocation(loc));
        config.save();
    }

    public void delHome(final String name) throws Exception {
        String search = getHomeName(name);
        if (!holder.homes().containsKey(search)) {
            search = StringUtil.safeString(search);
        }
        if (holder.homes().containsKey(search)) {
            holder.homes().remove(search);
            config.save();
        } else {
            throw new Exception(tl("invalidHome", search));
        }
    }

    public boolean hasHome() {
        return !holder.homes().isEmpty();
    }

    public boolean hasHome(final String name) {
        return holder.homes().containsKey(name);
    }

    public String getNickname() {
        return holder.nickname();
    }

    public void setNickname(final String nick) {
        holder.nickname(nick);
        config.save();
    }

    public Set<Material> getUnlimited() {
        return holder.unlimited();
    }

    public boolean hasUnlimited(final ItemStack stack) {
        return holder.unlimited().contains(stack.getType());
    }

    public void setUnlimited(final ItemStack stack, final boolean state) {
        final boolean wasUpdated;
        if (state) {
            wasUpdated = holder.unlimited().add(stack.getType());
        } else {
            wasUpdated = holder.unlimited().remove(stack.getType());
        }

        if (wasUpdated) {
            config.save();
        }
    }

    public Location getLastLocation() {
        final LazyLocation lastLocation = holder.lastLocation();
        return lastLocation != null ? lastLocation.location() : null;
    }

    public void setLastLocation(final Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        holder.lastLocation(loc);
        config.save();
    }

    public Location getLogoutLocation() {
        final LazyLocation logoutLocation = holder.logoutLocation();
        return logoutLocation != null ? logoutLocation.location() : null;
    }

    public void setLogoutLocation(final Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        holder.logoutLocation(loc);
        config.save();
    }

    public long getLastLogin() {
        return holder.timestamps().login();
    }

    public void setLastLogin(final long time) {
        holder.timestamps().login(time);
        if (base.getAddress() != null && base.getAddress().getAddress() != null) {
            holder.ipAddress(base.getAddress().getAddress().getHostAddress());
        }
        config.save();
    }

    public long getLastLogout() {
        return holder.timestamps().logout();
    }

    public void setLastLogout(final long time) {
        holder.timestamps().logout(time);
        config.save();
    }


    public boolean isNPC() {
        return holder.npc();
    }

    public void setNPC(final boolean set) {
        holder.npc(set);
        config.save();
    }

    public String getLastAccountName() {
        return holder.lastAccountName();
    }

    public void setLastAccountName(final String lastAccountName) {
        holder.lastAccountName(lastAccountName);
        config.save();
        redsmokes.getUserMap().trackUUID(getConfigUUID(), lastAccountName, true);
    }

    public List<CommandCooldown> getCooldownsList() {
        return holder.timestamps().commandCooldowns();
    }

    public Map<Pattern, Long> getCommandCooldowns() {
        final Map<Pattern, Long> map = new HashMap<>();
        for (final CommandCooldown c : getCooldownsList()) {
            if (c == null || c.isIncomplete()) {
                // stupid solution to stupid problem
                continue;
            }
            map.put(c.pattern(), c.value());
        }
        return map;
    }

    public Date getCommandCooldownExpiry(final String label) {
        for (CommandCooldown cooldown : getCooldownsList()) {
            if (cooldown == null || cooldown.isIncomplete()) {
                // stupid solution to stupid problem
                continue;
            }
            if (cooldown.pattern().matcher(label).matches()) {
                return new Date(cooldown.value());
            }
        }
        return null;
    }

    public void addCommandCooldown(final Pattern pattern, final Date expiresAt, final boolean save) {
        final CommandCooldown cooldown = new CommandCooldown();
        cooldown.pattern(pattern);
        cooldown.value(expiresAt.getTime());
        if (cooldown.isIncomplete()) {
            return;
        }
        holder.timestamps().commandCooldowns().add(cooldown);
        if (save) {
            save();
        }
    }

    public boolean clearCommandCooldown(final Pattern pattern) {
        if (holder.timestamps().commandCooldowns().isEmpty()) {
            return false; // false for no modification
        }

        if (getCooldownsList().removeIf(cooldown -> cooldown != null && !cooldown.isIncomplete() && cooldown.pattern().equals(pattern))) {
            save();
            return true;
        }
        return false;
    }

    public boolean isAcceptingPay() {
        return holder.acceptingPay();
    }

    public void setAcceptingPay(final boolean acceptingPay) {
        holder.acceptingPay(acceptingPay);
        save();
    }

    public boolean isPromptingPayConfirm() {
        return holder.confirmPay() != null ? holder.confirmPay() : redsmokes.getSettings().isConfirmCommandEnabledByDefault("pay");
    }

    public void setPromptingPayConfirm(final boolean prompt) {
        holder.confirmPay(prompt);
        save();
    }

    public boolean isBaltopExcludeCache() {
        return holder.baltopExempt();
    }

    public void setBaltopExemptCache(boolean baltopExempt) {
        holder.baltopExempt(baltopExempt);
        config.save();
    }

    public UUID getConfigUUID() {
        return config.getUuid();
    }

    public void save() {
        config.save();
    }

    public void startTransaction() {
        config.startTransaction();
    }

    public void stopTransaction() {
        config.stopTransaction();
    }

    public void setConfigProperty(String node, Object object) {
        setConfigPropertyRaw("info." + node, object);
    }

    public void setConfigPropertyRaw(String node, Object object) {
        config.setRaw(node, object);
        config.save();
    }

    public Set<String> getConfigKeys() {
        return ConfigurateUtil.getKeys(config.getSection("info"));
    }

    public Map<String, Object> getConfigMap() {
        return ConfigurateUtil.getRawMap(config.getSection("info"));
    }

    public Map<String, Object> getConfigMap(final String node) {
        return ConfigurateUtil.getRawMap(config.getSection("info." + node));
    }
}