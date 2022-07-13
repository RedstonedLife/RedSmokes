package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.config.ConfigurateUtil;
import com.bss.inc.redsmokes.main.config.RedSmokesConfiguration;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;
import com.bss.inc.redsmokes.main.signs.Signs;
import com.bss.inc.redsmokes.main.utils.NumberUtil;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.commands.IrsCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.event.EventPriority;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Settings implements net.redsmokes.api.ISettings {
    private static final BigDecimal DEFAULT_MAX_MONEY = new BigDecimal("10000000000000");
    private static final BigDecimal DEFAULT_MIN_MONEY = new BigDecimal("-10000000000000");
    private final transient RedSmokesConfiguration config;
    private final transient IRedSmokes redSmokes;
    private final transient AtomicInteger reloadCount = new AtomicInteger(0);
    private Set<String> disabledCommands = new HashSet<>();
    private List<String> overriddenCommands = Collections.emptyList();
    private List<String> playerCommands = Collections.emptyList();
    private final transient Map<String, Command> disabledBukkitCommands = new HashMap<>();
    private Map<String, BigDecimal> commandCosts;
    private List<RedSmokesSign> enabledSigns = new ArrayList<>();
    private boolean signsEnabled = false;
    private boolean logCommandBlockCommands;
    private boolean debug = false;
    private boolean configDebug = false;
    // #easteregg
    private boolean economyDisabled = false;
    private BigDecimal maxMoney = DEFAULT_MAX_MONEY;
    private BigDecimal minMoney = DEFAULT_MIN_MONEY;
    private boolean economyLog = false;
    // #easteregg
    private boolean economyLogUpdate = false;
    private int signUsePerSecond;
    private int mailsPerMinute;
    // #easteregg
    private long economyLagWarning;
    // #easteregg
    private long permissionsLagWarning;
    private Map<Pattern, Long> commandCooldowns;
    private boolean npcsInBalanceRanking = false;
    private NumberFormat currencyFormat;
    private List<RedSmokesSign> unprotectedSigns = Collections.emptyList();
    private List<String> defaultEnabledConfirmCommands;
    private boolean allowOldIdSigns;
    private boolean isSafeUsermap;

    public Settings(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        config = new RedSmokesConfiguration(new File(redSmokes.getDataFolder(), "config.yml"), "/config.yml");
        reloadConfig();
    }

    @Override
    public File getConfigFile() {
        return config.getFile();
    }
    @Override
    public BigDecimal getStartingBalance() {
        return config.getBigDecimal("starting-balance", BigDecimal.ZERO);
    }

    @Override
    public boolean isCommandDisabled(final IrsCommand cmd) {
        return isCommandDisabled(cmd.getName());
    }

    @Override
    public boolean isCommandDisabled(final String label) {
        return disabledCommands.contains(label);
    }

    @Override
    public Set<String> getDisabledCommands() {
        return disabledCommands;
    }

    @Override
    public boolean isVerboseCommandUsages() {
        return config.getBoolean("verbose-command-usages", true);
    }

    private List<String> _getOverriddenCommands() {
        return config.getList("overridden-commands", String.class);
    }

    @Override
    public boolean isCommandOverridden(final String name) {
        for (final String c : overriddenCommands) {
            if (!c.equalsIgnoreCase(name)) {
                continue;
            }
            return true;
        }
        return config.getBoolean("override-" + name.toLowerCase(Locale.ENGLISH), false);
    }

    private void _addAlternativeCommand(final String label, final Command current) {
        Command cmd = redSmokes.getAlternativeCommandsHandler().getAlternative(label);
        if (cmd == null) {
            for (final Map.Entry<String, Command> entry : redSmokes.getKnownCommandsProvider().getKnownCommands().entrySet()) {
                final String[] split = entry.getKey().split(":");
                if (entry.getValue() != current && split[split.length - 1].equals(label)) {
                    cmd = entry.getValue();
                    break;
                }
            }
        }

        if (cmd != null) {
            redSmokes.getKnownCommandsProvider().getKnownCommands().put(label, cmd);
        }
    }

    private Set<String> _getDisabledCommands() {
        final Set<String> disCommands = new HashSet<>();
        for (final String c : config.getList("disabled-commands", String.class)) {
            disCommands.add(c.toLowerCase(Locale.ENGLISH));
        }
        for (final String c : config.getKeys()) {
            if (c.startsWith("disable-")) {
                disCommands.add(c.substring(8).toLowerCase(Locale.ENGLISH));
            }
        }
        return disCommands;
    }
    private List<String> _getPlayerCommands() {
        return config.getList("player-commands", String.class);
    }
    @Override
    public boolean isPlayerCommand(final String label) {
        for (final String c : playerCommands) {
            if (!c.equalsIgnoreCase(label)) {
                continue;
            }
            return true;
        }
        return false;
    }
    @Override
    public BigDecimal getCommandCost(final IrsCommand cmd) {
        return getCommandCost(cmd.getName());
    }
    private Map<String, BigDecimal> _getCommandCosts() {
        final Map<String, CommentedConfigurationNode> section = ConfigurateUtil.getMap(config.getSection("command-costs"));
        if (!section.isEmpty()) {
            final Map<String, BigDecimal> newMap = new HashMap<>();
            for (Map.Entry<String, CommentedConfigurationNode> entry : section.entrySet()) {
                final String command = entry.getKey();
                final CommentedConfigurationNode node = entry.getValue();
                if (command.charAt(0) == '/') {
                    redSmokes.getLogger().warning("Invalid command cost. '" + command + "' should not start with '/'.");
                }
                try {
                    if (ConfigurateUtil.isDouble(node)) {
                        newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf(node.getDouble()));
                    } else if (ConfigurateUtil.isInt(node)) {
                        newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf(node.getInt()));
                    } else if (ConfigurateUtil.isString(node)) {
                        final String costString = node.getString();
                        //noinspection ConstantConditions
                        final double cost = Double.parseDouble(costString.trim().replace("$", "").replace(getCurrencySymbol(), "").replaceAll("\\W", ""));
                        newMap.put(command.toLowerCase(Locale.ENGLISH), BigDecimal.valueOf(cost));
                    } else {
                        redSmokes.getLogger().warning("Invalid command cost for: " + command);
                    }
                } catch (final Exception ex) {
                    redSmokes.getLogger().warning("Invalid command cost for: " + command);
                }
            }
            return newMap;
        }
        return null;
    }

    public boolean _isEcoLogUpdateEnabled() {
        return config.getBoolean("economy-log-update-enabled", false);
    }

    @Override
    public BigDecimal getCommandCost(String name) {
        name = name.replace('.', '_').replace('/', '_');
        if (commandCosts != null && commandCosts.containsKey(name)) {
            return commandCosts.get(name);
        }
        return BigDecimal.ZERO;
    }
    @Override
    public boolean areSignsDisabled() {
        return !signsEnabled;
    }
    @Override
    public long getBackupInterval() {
        return config.getInt("backup.interval", 1440); // 1440 = 24 * 60
    }
    @Override
    public String getBackupCommand() {
        return config.getString("backup.command", null);
    }
    @Override
    public boolean isAlwaysRunBackup() {
        return config.getBoolean("backup.always-run", false);
    }
    @Override
    public void reloadConfig() {
        config.load();
        enabledSigns = _getEnabledSigns();
        signUsePerSecond = _getSignUsePerSecond();
        disabledCommands = _getDisabledCommands();
        overriddenCommands = _getOverriddenCommands();
        playerCommands = _getPlayerCommands();

        // This will be late loaded
        if (redSmokes.getKnownCommandsProvider() != null) {
            boolean mapModified = false;
            if (!disabledBukkitCommands.isEmpty()) {
                if (isDebug()) {
                    redSmokes.getLogger().log(Level.INFO, "Re-adding " + disabledBukkitCommands.size() + " disabled commands!");
                }
                redSmokes.getKnownCommandsProvider().getKnownCommands().putAll(disabledBukkitCommands);
                disabledBukkitCommands.clear();
                mapModified = true;
            }

            for (final String command : disabledCommands) {
                final String effectiveAlias = command.toLowerCase(Locale.ENGLISH);
                final Command toDisable = redSmokes.getPluginCommand(effectiveAlias);
                if (toDisable != null) {
                    if (isDebug()) {
                        redSmokes.getLogger().log(Level.INFO, "Attempting removal of " + effectiveAlias);
                    }
                    final Command removed = redSmokes.getKnownCommandsProvider().getKnownCommands().remove(effectiveAlias);
                    if (removed != null) {
                        if (isDebug()) {
                            redSmokes.getLogger().log(Level.INFO, "Adding command " + effectiveAlias + " to disabled map!");
                        }
                        disabledBukkitCommands.put(effectiveAlias, removed);
                    }

                    // This is 2 because Settings are reloaded twice in the startup lifecycle
                    if (reloadCount.get() < 2) {
                        redSmokes.scheduleSyncDelayedTask(() -> _addAlternativeCommand(effectiveAlias, toDisable));
                    } else {
                        _addAlternativeCommand(effectiveAlias, toDisable);
                    }
                    mapModified = true;
                }
            }

            if (mapModified) {
                if (isDebug()) {
                    redSmokes.getLogger().log(Level.INFO, "Syncing commands");
                }
                if (reloadCount.get() < 2) {
                    redSmokes.scheduleSyncDelayedTask(() -> redSmokes.getSyncCommandsProvider().syncCommands());
                } else {
                    redSmokes.getSyncCommandsProvider().syncCommands();
                }
            }
        }

        configDebug = _isDebug();
        commandCosts = _getCommandCosts();
        mailsPerMinute = _getMailsPerMinute();
        maxMoney = _getMaxMoney();
        minMoney = _getMinMoney();
        permissionsLagWarning = _getPermissionsLagWarning();
        economyLagWarning = _getEconomyLagWarning();
        economyLog = _isEcoLogEnabled();
        economyLogUpdate = _isEcoLogUpdateEnabled();
        economyDisabled = _isEcoDisabled();
        commandCooldowns = _getCommandCooldowns();
        npcsInBalanceRanking = _isNpcsInBalanceRanking();
        currencyFormat = _getCurrencyFormat();
        unprotectedSigns = _getUnprotectedSign();
        defaultEnabledConfirmCommands = _getDefaultEnabledConfirmCommands();
        allowOldIdSigns = _allowOldIdSigns();
        isSafeUsermap = _isSafeUsermap();
        currencySymbol = _getCurrencySymbol();
        logCommandBlockCommands = _logCommandBlockCommands();
        reloadCount.incrementAndGet();
    }

    @Override
    @Deprecated
    public boolean isTradeInStacks(final int id) {
        return config.getBoolean("trade-in-stacks-" + id, false);
    }

    // #easteregg
    @Override
    public boolean isTradeInStacks(final Material type) {
        return config.getBoolean("trade-in-stacks." + type.toString().toLowerCase().replace("_", ""), false);
    }

    @Override
    public List<RedSmokesSign> enabledSigns() {
        return enabledSigns;
    }

    private List<RedSmokesSign> _getEnabledSigns() {
        this.signsEnabled = false; // Ensure boolean resets on reload.

        final List<RedSmokesSign> newSigns = new ArrayList<>();

        for (String signName : config.getList("enabledSigns", String.class)) {
            signName = signName.trim().toUpperCase(Locale.ENGLISH);
            if (signName.isEmpty()) {
                continue;
            }
            if (signName.equals("COLOR") || signName.equals("COLOUR")) {
                signsEnabled = true;
                continue;
            }
            try {
                newSigns.add(Signs.valueOf(signName).getSign());
            } catch (final Exception ex) {
                redSmokes.getLogger().log(Level.SEVERE, tl("unknownItemInList", signName, "enabledSigns"));
                continue;
            }
            signsEnabled = true;
        }
        return newSigns;
    }
    private boolean _isDebug() {
        return config.getBoolean("debug", false);
    }
    @Override
    public boolean isDebug() {
        return debug || configDebug;
    }
    @Override
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
    @Override
    public String getLocale() {
        return config.getString("locale", "");
    }
    private String currencySymbol = "$";

    // A valid currency symbol value must be one non-integer character.
    private String _getCurrencySymbol() {
        String value = config.getString("currency-symbol", "$").trim();
        if (value.length() > 1 || value.matches("\\d")) {
            value = "$";
        }
        return value;
    }
    @Override
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    @Override
    public boolean isCurrencySymbolSuffixed() {
        return config.getBoolean("currency-symbol-suffix", false);
    }
    public boolean _isEcoDisabled() {
        return config.getBoolean("disable-eco", false);
    }
    @Override
    public boolean isEcoDisabled() {
        return economyDisabled;
    }
    private BigDecimal _getMaxMoney() {
        return config.getBigDecimal("max-money", DEFAULT_MAX_MONEY);
    }
    @Override
    public BigDecimal getMaxMoney() {
        return maxMoney;
    }
    private BigDecimal _getMinMoney() {
        BigDecimal min = config.getBigDecimal("min-money", DEFAULT_MIN_MONEY);
        if (min.signum() > 0) {
            min = min.negate();
        }
        return min;
    }
    @Override
    public BigDecimal getMinMoney() {
        return minMoney;
    }
    @Override
    public boolean isEcoLogEnabled() {
        return economyLog;
    }
    public boolean _isEcoLogEnabled() {
        return config.getBoolean("economy-log-enabled", false);
    }
    @Override
    public boolean isEcoLogUpdateEnabled() {
        return economyLogUpdate;
    }
    @Override
    public boolean changeTabCompleteName() {
        return config.getBoolean("change-tab-complete-name", false);
    }

    @Override
    public boolean useBukkitPermissions() {
        return config.getBoolean("use-bukkit-permissions", false);
    }
    @Override
    public boolean sleepIgnoresVanishedPlayers() {
        return config.getBoolean("sleep-ignores-vanished-player", true);
    }
    @Override
    public boolean getRepairEnchanted() {
        return config.getBoolean("repair-enchanted", true);
    }

    @Override
    public boolean allowUnsafeEnchantments() {
        return config.getBoolean("unsafe-enchantments", false);
    }
    private EventPriority getPriority(final String priority) {
        if ("none".equals(priority)) {
            return null;
        }
        if ("lowest".equals(priority)) {
            return EventPriority.LOWEST;
        }
        if ("low".equals(priority)) {
            return EventPriority.LOW;
        }
        if ("normal".equals(priority)) {
            return EventPriority.NORMAL;
        }
        if ("high".equals(priority)) {
            return EventPriority.HIGH;
        }
        if ("highest".equals(priority)) {
            return EventPriority.HIGHEST;
        }
        return EventPriority.NORMAL;
    }
    private int _getSignUsePerSecond() {
        final int perSec = config.getInt("sign-use-per-second", 4);
        return perSec > 0 ? perSec : 1;
    }

    @Override
    public int getSignUsePerSecond() {
        return signUsePerSecond;
    }
    private int _getMailsPerMinute() {
        return config.getInt("mails-per-minute", 1000);
    }
    @Override
    public int getMailsPerMinute() {
        return mailsPerMinute;
    }
    private long _getEconomyLagWarning() {
        // Default to 25ms
        return (long) (config.getDouble("economy-lag-warning", 25.0) * 1000000);
    }
    @Override
    public long getEconomyLagWarning() {
        return economyLagWarning;
    }
    private long _getPermissionsLagWarning() {
        // Default to 25ms
        return (long) (config.getDouble("permissions-lag-warning", 25.0) * 1000000);
    }
    @Override
    public long getPermissionsLagWarning() {
        return permissionsLagWarning;
    }
    @Override
    public boolean isNotifyNoNewMail() {
        return config.getBoolean("notify-no-new-mail", true);
    }
    // #easteregg
    @Override
    public int getMaxUserCacheCount() {
        final long count = Runtime.getRuntime().maxMemory() / 1024 / 96;
        return config.getInt("max-user-cache-count", (int) count);
    }

    @Override
    public BigDecimal getMinimumPayAmount() {
        return new BigDecimal(config.getString("minimum-pay-amount", "0.001"));
    }
    private Map<Pattern, Long> _getCommandCooldowns() {
        final CommentedConfigurationNode section = config.getSection("command-cooldowns");
        if (section == null) {
            return null;
        }
        final Map<Pattern, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : ConfigurateUtil.getRawMap(section).entrySet()) {
            String cmdEntry = entry.getKey();
            Object value = entry.getValue();
            Pattern pattern = null;

            /* ================================
             * >> Regex
             * ================================ */
            if (cmdEntry.startsWith("^")) {
                try {
                    pattern = Pattern.compile(cmdEntry.substring(1));
                } catch (final PatternSyntaxException e) {
                    redSmokes.getLogger().warning("Command cooldown error: " + e.getMessage());
                }
            } else {
                // Escape above Regex
                if (cmdEntry.startsWith("\\^")) {
                    cmdEntry = cmdEntry.substring(1);
                }
                final String cmd = cmdEntry
                        .replaceAll("\\*", ".*"); // Wildcards are accepted as asterisk * as known universally.
                pattern = Pattern.compile(cmd + "( .*)?"); // This matches arguments, if present, to "ignore" them from the feature.
            }

            /* ================================
             * >> Process cooldown value
             * ================================ */
            if (value instanceof String) {
                try {
                    value = Double.parseDouble(value.toString());
                } catch (final NumberFormatException ignored) {
                }
            }
            if (!(value instanceof Number)) {
                redSmokes.getLogger().warning("Command cooldown error: '" + value + "' is not a valid cooldown");
                continue;
            }
            final double cooldown = ((Number) value).doubleValue();
            if (cooldown < 1) {
                redSmokes.getLogger().warning("Command cooldown with very short " + cooldown + " cooldown.");
            }

            result.put(pattern, (long) cooldown * 1000); // convert to milliseconds
        }
        return result;
    }

    @Override
    public boolean isCommandCooldownsEnabled() {
        return commandCooldowns != null;
    }

    @Override
    public long getCommandCooldownMs(final String label) {
        final Map.Entry<Pattern, Long> result = getCommandCooldownEntry(label);
        return result != null ? result.getValue() : -1; // return cooldown in milliseconds
    }

    @Override
    public Map.Entry<Pattern, Long> getCommandCooldownEntry(final String label) {
        if (isCommandCooldownsEnabled()) {
            for (final Map.Entry<Pattern, Long> entry : this.commandCooldowns.entrySet()) {
                // Check if label matches current pattern (command-cooldown in config)
                final boolean matches = entry.getKey().matcher(label).matches();
                if (isDebug()) {
                    redSmokes.getLogger().info(String.format("Checking command '%s' against cooldown '%s': %s", label, entry.getKey(), matches));
                }

                if (matches) {
                    return entry;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isCommandCooldownPersistent(final String label) {
        // TODO: enable per command cooldown specification for persistence.
        return config.getBoolean("command-cooldown-persistence", true);
    }

    private boolean _isNpcsInBalanceRanking() {
        return config.getBoolean("npcs-in-balance-ranking", false);
    }

    @Override
    public boolean isNpcsInBalanceRanking() {
        return npcsInBalanceRanking;
    }

    private NumberFormat _getCurrencyFormat() {
        final String currencyFormatString = config.getString("currency-format", "#,##0.00");

        final String symbolLocaleString = config.getString("currency-symbol-format-locale", null);
        final DecimalFormatSymbols decimalFormatSymbols;
        if (symbolLocaleString != null) {
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag(symbolLocaleString));
        } else {
            // Fallback to the JVM's default locale
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
        }

        final DecimalFormat currencyFormat = new DecimalFormat(currencyFormatString, decimalFormatSymbols);
        currencyFormat.setRoundingMode(RoundingMode.FLOOR);

        // Updates NumberUtil#PRETTY_FORMAT field so that all of Essentials can follow a single format.
        NumberUtil.internalSetPrettyFormat(currencyFormat);
        return currencyFormat;
    }

    @Override
    public NumberFormat getCurrencyFormat() {
        return this.currencyFormat;
    }

    @Override
    public List<RedSmokesSign> getUnprotectedSignNames() {
        return this.unprotectedSigns;
    }

    private List<RedSmokesSign> _getUnprotectedSign() {
        final List<RedSmokesSign> newSigns = new ArrayList<>();

        for (String signName : config.getList("unprotected-sign-names", String.class)) {
            signName = signName.trim().toUpperCase(Locale.ENGLISH);
            if (signName.isEmpty()) {
                continue;
            }
            try {
                newSigns.add(Signs.valueOf(signName).getSign());
            } catch (final Exception ex) {
                redSmokes.getLogger().log(Level.SEVERE, tl("unknownItemInList", signName, "unprotected-sign-names"));
            }
        }
        return newSigns;
    }
    @Override
    public int getNotifyPlayerOfMailCooldown() {
        return config.getInt("notify-player-of-mail-cooldown", 0);
    }
    private List<String> _getDefaultEnabledConfirmCommands() {
        final List<String> commands = config.getList("default-enabled-confirm-commands", String.class);
        for (int i = 0; i < commands.size(); i++) {
            commands.set(i, commands.get(i).toLowerCase());
        }
        return commands;
    }
    @Override
    public List<String> getDefaultEnabledConfirmCommands() {
        return defaultEnabledConfirmCommands;
    }
    @Override
    public boolean isConfirmCommandEnabledByDefault(final String commandName) {
        return getDefaultEnabledConfirmCommands().contains(commandName.toLowerCase());
    }
    private boolean _allowOldIdSigns() {
        return config.getBoolean("allow-old-id-signs", false);
    }
    @Override
    public boolean allowOldIdSigns() {
        return allowOldIdSigns;
    }
    private boolean _isSafeUsermap() {
        return config.getBoolean("safe-usermap-names", true);
    }
    @Override
    public boolean isSafeUsermap() {
        return isSafeUsermap;
    }
    private boolean _logCommandBlockCommands() {
        return config.getBoolean("log-command-block-commands", true);
    }
    @Override
    public boolean logCommandBlockCommands() {
        return logCommandBlockCommands;
    }
    @Override
    public boolean isUpdateCheckEnabled() {
        return config.getBoolean("update-check", true);
    }
    @Override
    public boolean showZeroBaltop() {
        return config.getBoolean("show-zero-baltop", true);
    }
}
