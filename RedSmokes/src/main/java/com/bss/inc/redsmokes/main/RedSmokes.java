package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.OfflinePlayer;
import com.bss.inc.redsmokes.main.api.Economy;
import com.bss.inc.redsmokes.main.commands.*;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.economy.vault.VaultEconomyProvider;
import com.bss.inc.redsmokes.main.items.AbstractItemDb;
import com.bss.inc.redsmokes.main.items.CustomItemResolver;
import com.bss.inc.redsmokes.main.items.FlatItemDb;
import com.bss.inc.redsmokes.main.items.LegacyItemDb;
import com.bss.inc.redsmokes.main.metrics.MetricsWrapper;
import com.bss.inc.redsmokes.main.nms.refl.providers.*;
import com.bss.inc.redsmokes.main.perm.PermissionsDefaults;
import com.bss.inc.redsmokes.main.perm.PermissionsHandler;
import com.bss.inc.redsmokes.main.provider.*;
import com.bss.inc.redsmokes.main.provider.providers.*;
import com.bss.inc.redsmokes.main.signs.SignBlockListener;
import com.bss.inc.redsmokes.main.signs.SignEntityListener;
import com.bss.inc.redsmokes.main.signs.SignPlayerListener;
import com.bss.inc.redsmokes.main.updatecheck.UpdateChecker;
import com.bss.inc.redsmokes.main.utils.FormatUtil;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import com.bss.inc.redsmokes.main.utils.logging.BaseLoggerProvider;
import io.papermc.lib.PaperLib;
import net.redsmokes.api.IConf;
import net.redsmokes.api.IItemDb;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.ISettings;
import net.redsmokes.api.commands.IrsCommand;
import net.redsmokes.api.services.BalanceTop;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class RedSmokes extends JavaPlugin implements IRedSmokes {

    private static final Logger BUKKIT_LOGGER = Logger.getLogger("RedSmokes");
    private static Logger LOGGER = null;
    private transient ISettings settings;
    private transient Worth worth;
    private transient List<IConf> confList;
    private transient Backup backup;
    private transient AbstractItemDb itemDb;
    private transient CustomItemResolver customItemResolver;
    private transient PermissionsHandler permissionsHandler;
    private transient AlternativeCommandsHandler alternativeCommandsHandler;
    private transient UserMap userMap;
    private transient BalanceTopImpl balanceTop;
    private transient ExecuteTimer execTimer;
    private transient I18n i18n;
    private transient MetricsWrapper metrics;
    private transient RedSmokesTimer timer;
    private transient SpawnerItemProvider spawnerItemProvider;
    private transient SpawnerBlockProvider spawnerBlockProvider;
    private transient SpawnEggProvider spawnEggProvider;
    private transient PotionMetaProvider potionMetaProvider;
    private transient ServerStateProvider serverStateProvider;
    private transient ContainerProvider containerProvider;
    private transient SerializationProvider serializationProvider;
    private transient KnownCommandsProvider knownCommandsProvider;
    private transient FormattedCommandAliasProvider formattedCommandAliasProvider;
    private transient ProviderListener recipeBookEventProvider;
    private transient MaterialTagProvider materialTagProvider;
    private transient SyncCommandsProvider syncCommandsProvider;
    private transient PersistentDataProvider persistentDataProvider;
    private transient ReflOnlineModeProvider onlineModeProvider;
    private transient ItemUnbreakableProvider unbreakableProvider;
    private transient WorldInfoProvider worldInfoProvider;
    private transient SignDataProvider signDataProvider;
    private transient UpdateChecker updateChecker;
    private final transient Map<String, IrsCommand> commandMap = new HashMap<>();

    static {
        EconomyLayers.init();
    }

    public RedSmokes() {}

    protected RedSmokes(final JavaPluginLoader loader, final PluginDescriptionFile description, final File dataFolder, final File file) {
        super(loader, description, dataFolder, file);
    }

    public RedSmokes(final Server server) {
        super(new JavaPluginLoader(server), new PluginDescriptionFile("RedSmokes", "", "com.bss.inc.redsmokes.main.RedSmokes"), null, null);
    }

    @Override
    public ISettings getSettings() {return settings;}

    public void setupForTesting(final Server server) throws IOException, InvalidDescriptionException {
        LOGGER = new BaseLoggerProvider(this, BUKKIT_LOGGER);
        final File dataFolder = File.createTempFile("redsmokestest", "");
        if(!dataFolder.delete()) {throw new IOException();}
        if(!dataFolder.mkdir()) {throw new IOException();}
        i18n = new I18n(this);
        i18n.onEnable();
        i18n.updateLocale("en");
        Console.setInstance(this);

        LOGGER.log(Level.INFO, tl("usingTempFolderForTesting"));
        LOGGER.log(Level.INFO, dataFolder.toString());
        settings = new Settings(this);
        userMap = new UserMap(this);
        balanceTop = new BalanceTopImpl(this);
        permissionsHandler = new PermissionsHandler(this, false);
        Economy.setEss(this);
        confList = new ArrayList<>();
        registerListeners(server.getPluginManager());
    }

    @Override
    public void onLoad() {
        try {
            // Vault registers their RedSmokes provider at low priority, so we have to use normal priority here
            Class.forName("net.milkbowl.vault.economy.Economy");
            getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, new VaultEconomyProvider(this), this, ServicePriority.Normal);
        } catch (final ClassNotFoundException ignored) {} // Safer than fetching for the plugin as bukkit may not have marked it as enabled at this point in time
    }

    @Override
    public void onEnable() {
        try {
            if(BUKKIT_LOGGER != super.getLogger()) {
                BUKKIT_LOGGER.setParent(super.getLogger());
            }
            LOGGER = RedSmokesLogger.getLoggerProvider(this);
            RedSmokesLogger.updatePluginLogger(this);

            execTimer = new ExecuteTimer();
            execTimer.start();
            i18n = new I18n(this);
            i18n.onEnable();
            execTimer.mark("I18n1");

            Console.setInstance(this);

            switch (VersionUtil.getServerSupportStatus()) {
                case NMS_CLEANROOM:
                    getLogger().severe(tl("serverUnsupportedCleanroom"));
                    break;
                case DANGEROUS_FORK:
                    getLogger().severe(tl("serverUnsupportedDangerous"));
                    break;
                case UNSTABLE:
                    getLogger().severe(tl("serverUnsupportedMods"));
                    break;
                case OUTDATED:
                    getLogger().severe(tl("serverUnsupported"));
                    break;
                case LIMITED:
                    getLogger().info(tl("serverUnsupportedLimitedApi"));
                    break;
            }

            if(VersionUtil.getSupportStatusClass() != null) {
                getLogger().info(tl("serverUnsupportedClass", VersionUtil.getSupportStatusClass()));
            }

            final PluginManager pm = getServer().getPluginManager();
            for(final Plugin plugin : pm.getPlugins()) {
                if (plugin.getDescription().getName().startsWith("RedSmokes") && !plugin.getDescription().getVersion().equals(this.getDescription().getVersion())) {
                    getLogger().warning(tl("versionMismatch", plugin.getDescription().getName()));
                }
            }

            final RedSmokesUpgrade upgrade = new RedSmokesUpgrade(this);
            upgrade.beforeSettings();
            execTimer.mark("Upgrade");

            confList = new ArrayList<>();
            settings = new Settings(this);
            confList.add(settings);
            execTimer.mark("Settings");

            /**
             * Mail Service Implementation (Will be re-worked based on the EssentialsX mail service but with an added
             * touch, instead of being "username" reliant, It will be e-mail address reliant that will be attached to
             * your Minecraft UUID, Upon joining if you would like to receive emails you can register an email address
             * in-game it will be GUI based, in-console it will look like so /register-mail (provider) (username)
             * example: /register-mail mc-mail.com tal.baskin, the following email address will be attached to your
             * UUID and look in-game like so <br>tal.baskin@mc-mail.com</br>, Users can create email providers, and charge
             * per mail the limit is 0.01 c$ up to 0.15 c$ per mail, each person can create a different provider with its own limits
             * of course it will cost the register the provider, and every feature will cost money, the following pricing scheme (by default) is this
             * Create Provider - $1,500
             * Domain Registering - .xyz (150$), .org (950$), .edu (4,500$) .gov (15,000$ requires redsmokes.mail.gov), .net (17,500$) .com (20,500$)
             * Mail Size - 25 characters (100$) 50 characters (200$), 100 characters (300$) 250 characters (500$) 512 characters (1,250$)
             * Digital Signing (Cryptographically Signing) <- This will authenticate messages so no one can fake a message, 8,000$
             * Location Signing <- This will sign an email with the X,Y,Z,Pitch,Yaw & World Information, 2,500$
             * Client Storage (How many mails each client can store), 15 (100$), 30 (200$), 50 (500$), 100 (1,000$), UNLIMITED (15,000$, requires redsmokes.mail.no_limit)
             *
             * Example Emails Of Every Domain:
             * (Provider Name will be <br>mc-mail</br> for the examples)
             * tal.baskin@mc-mail.xyz
             * tal.baskin@mc-mail.org
             * tal.baskin@mc-mail.edu
             * tal.baskin@mc-mail.gov
             * tal.baskin@mc-mail.net
             * tal.baskin@mc-mail.com
             * When opening the email in game, you will be able to browse through the emails in a GUI, each email is a GUI containing 6 items
             * Item #1 - Date Information (When it was sent)
             * Item #2 - User Information (From Who: To Who:)
             * Item #3 - Digital Signature / Location Signature (If provider paid for them)
             * Item #4 - Reply To Mail
             * Item #5 - View Content (Book & Quil GUI)
             * Item #6 - Delete Mail
             *
             *
             * Note, Made by Tal A. Baskin (13th of July, 2022. 4:56 AM)
             * If at some point I'll have enough time, I'll try to actually make this a SMTP Mail Manager so theoretically I could give it a domain I own
             * for example, example.com. And users/admins can register a mail under it, so if an admin wants to send an email (I will add a discord-interpreter)
             * so users could register an email from the discord, like so /register-email <b>USERNAME</b> <b>PASSWORD</b> (In dms for safety)
             * and send mails to actual mail services like gmail, hotmail, protonmail just to name a few. If I could actually implement this
             * I would offer hosting for the SMTP side on my servers to off-load servers.
             */

            userMap = new UserMap(this);
            confList.add(userMap);
            execTimer.mark("Init(Usermap)");

            balanceTop = new BalanceTopImpl(this);
            execTimer.mark("Init(BalanceTop)");

            upgrade.afterSettings();
            execTimer.mark("Upgrade2");

            worth = new Worth(this.getDataFolder());
            confList.add(worth);
            execTimer.mark("Init(Worth)");

            itemDb = getItemDbFromConfig();
            confList.add(itemDb);
            execTimer.mark("Init(ItemDB)");

            customItemResolver = new CustomItemResolver(this);
            try {
                itemDb.registerResolver(this, "custom_items", customItemResolver);
                confList.add(customItemResolver);
            } catch (final Exception e) {
                e.printStackTrace();
                customItemResolver = null;
            }
            execTimer.mark("Init(CustomItemResolver)");

            EconomyLayers.onEnable(this);

            //Spawner item provider only uses one but it's here for legacy...
            spawnerItemProvider = new BlockMetaSpawnerItemProvider();

            //Spawner block providers
            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_12_0_R01)) {
                spawnerBlockProvider = new ReflSpawnerBlockProvider();
            } else {
                spawnerBlockProvider = new BukkitSpawnerBlockProvider();
            }

            //Spawn Egg Providers
            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_9_R01)) {
                spawnEggProvider = new LegacySpawnEggProvider();
            } else if (VersionUtil.getServerBukkitVersion().isLowerThanOrEqualTo(VersionUtil.v1_12_2_R01)) {
                spawnEggProvider = new ReflSpawnEggProvider();
            } else {
                spawnEggProvider = new FlatSpawnEggProvider();
            }

            //Potion Meta Provider
            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_9_R01)) {
                potionMetaProvider = new LegacyPotionMetaProvider();
            } else {
                potionMetaProvider = new BasePotionDataProvider();
            }

            //Server State Provider
            //Container Provider
            if (PaperLib.isPaper() && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_15_2_R01)) {
                serverStateProvider = new PaperServerStateProvider();
                containerProvider = new PaperContainerProvider();
                serializationProvider = new PaperSerializationProvider();
            } else {
                serverStateProvider = new ReflServerStateProvider();
            }

            //Event Providers
            if (PaperLib.isPaper()) {
                try {
                    Class.forName("com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent");
                    recipeBookEventProvider = new PaperRecipeBookListener(event -> {
                        if (this.getUser(((PlayerEvent) event).getPlayer()).isRecipeSee()) {
                            ((Cancellable) event).setCancelled(true);
                        }
                    });
                } catch (final ClassNotFoundException ignored) {
                }
            }

            //Known Commands Provider
            if (PaperLib.isPaper() && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_11_2_R01)) {
                knownCommandsProvider = new PaperKnownCommandsProvider();
            } else {
                knownCommandsProvider = new ReflKnownCommandsProvider();
            }

            // Command aliases provider
            formattedCommandAliasProvider = new ReflFormattedCommandAliasProvider(PaperLib.isPaper());

            // Material Tag Providers
            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_13_0_R01)) {
                materialTagProvider = PaperLib.isPaper() ? new PaperMaterialTagProvider() : new BukkitMaterialTagProvider();
            }

            // Sync Commands Provider
            syncCommandsProvider = new ReflSyncCommandsProvider();

            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_14_4_R01)) {
                persistentDataProvider = new ModernPersistentDataProvider(this);
            } else {
                persistentDataProvider = new ReflPersistentDataProvider(this);
            }

            onlineModeProvider = new ReflOnlineModeProvider();

            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_11_2_R01)) {
                unbreakableProvider = new ModernItemUnbreakableProvider();
            } else {
                unbreakableProvider = new LegacyItemUnbreakableProvider();
            }

            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_17_1_R01)) {
                worldInfoProvider = new ModernDataWorldInfoProvider();
            } else if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_16_5_R01)) {
                worldInfoProvider = new ReflDataWorldInfoProvider();
            } else {
                worldInfoProvider = new FixedHeightWorldInfoProvider();
            }

            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_14_4_R01)) {
                signDataProvider = new ModernSignDataProvider(this);
            }

            execTimer.mark("Init(Providers)");
            reload();

            backup = new Backup(this);
            permissionsHandler = new PermissionsHandler(this, settings.useBukkitPermissions());
            alternativeCommandsHandler = new AlternativeCommandsHandler(this);

            timer = new RedSmokesTimer(this);
            scheduleSyncRepeatingTask(timer, 1000, 50);

            Economy.setEss(this);
            execTimer.mark("RegHandler");

            // Register /back default permissions
            PermissionsDefaults.registerAllBackDefaults();

            updateChecker = new UpdateChecker(this);
            runTaskAsynchronously(() -> {
                getLogger().log(Level.INFO, tl("versionFetching"));
                for (String str : updateChecker.getVersionMessages(false, true)) {
                    getLogger().log(getSettings().isUpdateCheckEnabled() ? Level.WARNING : Level.INFO, str);
                }
            });

            metrics = new MetricsWrapper(this, 0, true);

            execTimer.mark("Init(External)");

            final String timeroutput = execTimer.end();
            if (getSettings().isDebug()) {
                LOGGER.log(Level.INFO, "RedSmokes load " + timeroutput);
            }

        } catch (final NumberFormatException ex) {
            handleCrash(ex);
        } catch (final Error ex) {
            handleCrash(ex);
            throw ex;
        }
        getBackup().setPendingShutdown(false);
    }

    public static Logger getWrappedLogger() {
        if (LOGGER != null) {
            return LOGGER;
        }

        return BUKKIT_LOGGER;
    }

    @Override
    public void saveConfig() {
        // We don't use any of the bukkit config writing, as this breaks our config file formatting.
    }

    private void registerListeners(final PluginManager pm) {
        HandlerList.unregisterAll(this);

        if (getSettings().isDebug()) {
            LOGGER.log(Level.INFO, "Registering Listeners");
        }

        final RedSmokesPluginListener pluginListener = new RedSmokesPluginListener(this);
        pm.registerEvents(pluginListener, this);
        confList.add(pluginListener);

        final RedSmokesPlayerListener playerListener = new RedSmokesPlayerListener(this);
        playerListener.registerEvents();

        final RedSmokesBlockListener blockListener = new RedSmokesBlockListener(this);
        pm.registerEvents(blockListener, this);

        final SignBlockListener signBlockListener = new SignBlockListener(this);
        pm.registerEvents(signBlockListener, this);

        final SignPlayerListener signPlayerListener = new SignPlayerListener(this);
        pm.registerEvents(signPlayerListener, this);

        final SignEntityListener signEntityListener = new SignEntityListener(this);
        pm.registerEvents(signEntityListener, this);

        final RedSmokesEntityListener entityListener = new RedSmokesEntityListener(this);
        pm.registerEvents(entityListener, this);

        final RedSmokesWorldListener worldListener = new RedSmokesWorldListener(this);
        pm.registerEvents(worldListener, this);

        final RedSmokesServerListener serverListener = new RedSmokesServerListener(this);
        pm.registerEvents(serverListener, this);

        if (recipeBookEventProvider != null) {
            pm.registerEvents(recipeBookEventProvider, this);
        }
    }

    @Override
    public void onDisable() {
        final boolean stopping = getServerStateProvider().isStopping();
        if (!stopping) {
            LOGGER.log(Level.SEVERE, tl("serverReloading"));
        }
        getBackup().setPendingShutdown(true);
        for (final User user : getOnlineUsers()) {
            if (user.isVanished()) {
                user.setVanished(false);
                user.sendMessage(tl("unvanishedReload"));
            }
            if (stopping) {
                user.setLogoutLocation(user.getLocation());
                if (!user.isHidden()) {
                    user.setLastLogout(System.currentTimeMillis());
                }
                user.cleanup();
            } else {
                user.stopTransaction();
            }
        }
        if (getBackup().getTaskLock() != null && !getBackup().getTaskLock().isDone()) {
            LOGGER.log(Level.SEVERE, tl("backupInProgress"));
            getBackup().getTaskLock().join();
        }
        if (i18n != null) {
            i18n.onDisable();
        }
        if (backup != null) {
            backup.stopTask();
        }

        this.getPermissionsHandler().unregisterContexts();

        Economy.setEss(null);
        Trade.closeLog();
        getUserMap().getUUIDMap().shutdown();

        HandlerList.unregisterAll(this);
    }

    @Override
    public void reload() {
        Trade.closeLog();

        for (final IConf iConf : confList) {
            iConf.reloadConfig();
            execTimer.mark("Reload(" + iConf.getClass().getSimpleName() + ")");
        }

        i18n.updateLocale(settings.getLocale());
        for (final String commandName : this.getDescription().getCommands().keySet()) {
            final Command command = this.getCommand(commandName);
            if (command != null) {
                command.setDescription(tl(commandName + "CommandDescription"));
                command.setUsage(tl(commandName + "CommandUsage"));
            }
        }

        final PluginManager pm = getServer().getPluginManager();
        registerListeners(pm);
    }

    private IrsCommand loadCommand(final String path, final String name, final IRedSmokesModule module, final ClassLoader classLoader) throws Exception {
        if (commandMap.containsKey(name)) {
            return commandMap.get(name);
        }
        final IrsCommand cmd = (IrsCommand) classLoader.loadClass(path + name).getDeclaredConstructor().newInstance();
        cmd.setEssentials(this);
        cmd.setEssentialsModule(module);
        commandMap.put(name, cmd);
        return cmd;
    }

    public Map<String, IrsCommand> getCommandMap() {
        return commandMap;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        return onTabCompleteRedSmokes(sender, command, commandLabel, args, RedSmokes.class.getClassLoader(),
                "com.bss.inc.redsmokes.main.commands.Command", "redsmokes.", null);
    }

    @Override
    public List<String> onTabCompleteRedSmokes(final CommandSender cSender, final Command command, final String commandLabel, final String[] args,
                                                final ClassLoader classLoader, final String commandPath, final String permissionPrefix,
                                                final IRedSmokesModule module) {
        if (!getSettings().isCommandOverridden(command.getName()) && (!commandLabel.startsWith("e") || commandLabel.equalsIgnoreCase(command.getName()))) {
            final Command pc = alternativeCommandsHandler.getAlternative(commandLabel);
            if (pc instanceof PluginCommand) {
                try {
                    final TabCompleter completer = ((PluginCommand) pc).getTabCompleter();
                    if (completer != null) {
                        return completer.onTabComplete(cSender, command, commandLabel, args);
                    }
                } catch (final Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }

        try {
            // Note: The tab completer is always a player, even when tab-completing in a command block
            User user = null;
            if (cSender instanceof Player) {
                user = getUser((Player) cSender);
            }

            final CommandSource sender = new CommandSource(cSender);

            // Check for disabled commands
            if (getSettings().isCommandDisabled(commandLabel)) {
                if (getKnownCommandsProvider().getKnownCommands().containsKey(commandLabel)) {
                    final Command newCmd = getKnownCommandsProvider().getKnownCommands().get(commandLabel);
                    if (!(newCmd instanceof PluginIdentifiableCommand) || ((PluginIdentifiableCommand) newCmd).getPlugin() != this) {
                        return newCmd.tabComplete(cSender, commandLabel, args);
                    }
                }
                return Collections.emptyList();
            }

            final IrsCommand cmd;
            try {
                cmd = loadCommand(commandPath, command.getName(), module, classLoader);
            } catch (final Exception ex) {
                sender.sendMessage(tl("commandNotLoaded", commandLabel));
                LOGGER.log(Level.SEVERE, tl("commandNotLoaded", commandLabel), ex);
                return Collections.emptyList();
            }

            // Check authorization
            if (user != null && !user.isAuthorized(cmd, permissionPrefix)) {
                return Collections.emptyList();
            }


            // Run the command
            try {
                if (user == null) {
                    return cmd.tabComplete(getServer(), sender, commandLabel, command, args);
                } else {
                    return cmd.tabComplete(getServer(), user, commandLabel, command, args);
                }
            } catch (final Exception ex) {
                showError(sender, ex, commandLabel);
                // Tab completion shouldn't fail
                LOGGER.log(Level.SEVERE, tl("commandFailed", commandLabel), ex);
                return Collections.emptyList();
            }
        } catch (final Throwable ex) {
            LOGGER.log(Level.SEVERE, tl("commandFailed", commandLabel), ex);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        metrics.markCommand(command.getName(), true);
        return onCommandRedSmokes(sender, command, commandLabel, args, RedSmokes.class.getClassLoader(), "com.bss.inc.redsmokes.main.commands.Command", "redsmokes.", null);
    }

    @Override
    public boolean onCommandRedSmokes(final CommandSender cSender, final Command command, final String commandLabel, final String[] args, final ClassLoader classLoader, final String commandPath, final String permissionPrefix, final IRedSmokesModule module) {
        // Allow plugins to override the command via onCommand
        if (!getSettings().isCommandOverridden(command.getName()) && (!commandLabel.startsWith("r") || commandLabel.equalsIgnoreCase(command.getName()))) {
            if (getSettings().isDebug()) {
                LOGGER.log(Level.INFO, "Searching for alternative to: " + commandLabel);
            }
            final Command pc = alternativeCommandsHandler.getAlternative(commandLabel);
            if (pc != null) {
                alternativeCommandsHandler.executed(commandLabel, pc);
                try {
                    pc.execute(cSender, commandLabel, args);
                } catch (final Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    cSender.sendMessage(tl("internalError"));
                }
                return true;
            }
        }

        try {

            User user = null;
            Block bSenderBlock = null;
            if (cSender instanceof Player) {
                user = getUser((Player) cSender);
            } else if (cSender instanceof BlockCommandSender) {
                final BlockCommandSender bsender = (BlockCommandSender) cSender;
                bSenderBlock = bsender.getBlock();
            }

            if (bSenderBlock != null) {
                if (getSettings().logCommandBlockCommands()) {
                    LOGGER.log(Level.INFO, "CommandBlock at " + bSenderBlock.getX() + "," + bSenderBlock.getY() + "," + bSenderBlock.getZ() + " issued server command: /" + commandLabel + " " + RedSmokesCommand.getFinalArg(args, 0));
                }
            } else if (user == null) {
                LOGGER.log(Level.INFO, cSender.getName()+ " issued server command: /" + commandLabel + " " + RedSmokesCommand.getFinalArg(args, 0));
            }

            final CommandSource sender = new CommandSource(cSender);

            //Print version even if admin command is not available #easteregg
            if (commandLabel.equalsIgnoreCase("redversion")) {
                sender.sendMessage("This server is running RedSmokes " + getDescription().getVersion());
                return true;
            }

            // Check for disabled commands
            if (getSettings().isCommandDisabled(commandLabel)) {
                if (getKnownCommandsProvider().getKnownCommands().containsKey(commandLabel)) {
                    final Command newCmd = getKnownCommandsProvider().getKnownCommands().get(commandLabel);
                    if (!(newCmd instanceof PluginIdentifiableCommand) || !isRedSmokesPlugin(((PluginIdentifiableCommand) newCmd).getPlugin())) {
                        return newCmd.execute(cSender, commandLabel, args);
                    }
                }
                sender.sendMessage(tl("commandDisabled", commandLabel));
                return true;
            }

            final IrsCommand cmd;
            try {
                cmd = loadCommand(commandPath, command.getName(), module, classLoader);
            } catch (final Exception ex) {
                sender.sendMessage(tl("commandNotLoaded", commandLabel));
                LOGGER.log(Level.SEVERE, tl("commandNotLoaded", commandLabel), ex);
                return true;
            }

            // Check authorization
            if (user != null && !user.isAuthorized(cmd, permissionPrefix)) {
                LOGGER.log(Level.INFO, tl("deniedAccessCommand", user.getName()));
                user.sendMessage(tl("noAccessCommand"));
                return true;
            }

            // Run the command
            try {
                if (user == null) {
                    cmd.run(getServer(), sender, commandLabel, command, args);
                } else {
                    cmd.run(getServer(), user, commandLabel, command, args);
                }
                return true;
            } catch (final NoChargeException | QuietAbortException ex) {
                return true;
            } catch (final NotEnoughArgumentsException ex) {
                if (getSettings().isVerboseCommandUsages() && !cmd.getUsageStrings().isEmpty()) {
                    sender.sendMessage(tl("commandHelpLine1", commandLabel));
                    sender.sendMessage(tl("commandHelpLine2", command.getDescription()));
                    sender.sendMessage(tl("commandHelpLine3"));
                    for (Map.Entry<String, String> usage : cmd.getUsageStrings().entrySet()) {
                        sender.sendMessage(tl("commandHelpLineUsage", usage.getKey().replace("<command>", commandLabel), usage.getValue()));
                    }
                } else {
                    sender.sendMessage(command.getDescription());
                    sender.sendMessage(command.getUsage().replace("<command>", commandLabel));
                }
                if (!ex.getMessage().isEmpty()) {
                    sender.sendMessage(ex.getMessage());
                }
                if (ex.getCause() != null && settings.isDebug()) {
                    ex.getCause().printStackTrace();
                }
                return true;
            } catch (final Exception ex) {
                showError(sender, ex, commandLabel);
                if (settings.isDebug()) {
                    ex.printStackTrace();
                }
                return true;
            }
        } catch (final Throwable ex) {
            LOGGER.log(Level.SEVERE, tl("commandFailed", commandLabel), ex);
            return true;
        }
    }




    private void handleCrash(final Throwable exception) {
        final PluginManager pm = getServer().getPluginManager();
        LOGGER.log(Level.SEVERE, exception.toString());
        exception.printStackTrace();
        pm.registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOW)
            public void onPlayerJoin(final PlayerJoinEvent event) {
                event.getPlayer().sendMessage("RedSmokes failed to load, read the log file.");
            }
        }, this);
        for (final Player player : getOnlinePlayers()) {
            player.sendMessage("RedSmokes failed to load, read the log file.");
        }
        this.setEnabled(false);
    }

    private boolean isRedSmokesPlugin(Plugin plugin) {
        return plugin.getDescription().getMain().contains("com.bss.inc.redsmokes") || plugin.getDescription().getMain().contains("net.redsmokes");
    }

    @Override
    public void showError(final CommandSource sender, final Throwable exception, final String commandLabel) {
        sender.sendMessage(tl("errorWithMessage", exception.getMessage()));
        if (getSettings().isDebug()) {
            LOGGER.log(Level.INFO, tl("errorCallingCommand", commandLabel), exception);
        }
    }

    @Override
    public BukkitScheduler getScheduler() {
        return this.getServer().getScheduler();
    }

    @Override
    public Worth getWorth() {
        return worth;
    }

    @Override
    public Backup getBackup() {
        return backup;
    }


    @Override
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Deprecated
    @Override
    public User getUser(final Object base) {
        if (base instanceof Player) {
            return getUser((Player) base);
        }
        if (base instanceof org.bukkit.OfflinePlayer) {
            return getUser(((org.bukkit.OfflinePlayer) base).getUniqueId());
        }
        if (base instanceof UUID) {
            return getUser((UUID) base);
        }
        if (base instanceof String) {
            return getOfflineUser((String) base);
        }
        return null;
    }

    //This will return null if there is not a match.
    @Override
    public User getUser(final String base) {
        return getOfflineUser(base);
    }

    //This will return null if there is not a match.
    @Override
    public User getUser(final UUID base) {
        return userMap.getUser(base);
    }

    //This will return null if there is not a match.
    @Override
    public User getOfflineUser(final String name) {
        final User user = userMap.getUser(name);
        if (user != null && user.getBase() instanceof OfflinePlayer) {
            //This code should attempt to use the last known name of a user, if Bukkit returns name as null.
            final String lastName = user.getLastAccountName();
            if (lastName != null) {
                ((OfflinePlayer) user.getBase()).setName(lastName);
            } else {
                ((OfflinePlayer) user.getBase()).setName(name);
            }
        }
        return user;
    }

    @Override
    public User matchUser(final Server server, final User sourceUser, final String searchTerm, final Boolean getHidden, final boolean getOffline) throws PlayerNotFoundException {
        final User user;
        Player exPlayer;

        try {
            exPlayer = server.getPlayer(UUID.fromString(searchTerm));
        } catch (final IllegalArgumentException ex) {
            if (getOffline) {
                exPlayer = server.getPlayerExact(searchTerm);
            } else {
                exPlayer = server.getPlayer(searchTerm);
            }
        }

        if (exPlayer != null) {
            user = getUser(exPlayer);
        } else {
            user = getUser(searchTerm);
        }

        if (user != null) {
            if (!getOffline && !user.getBase().isOnline()) {
                throw new PlayerNotFoundException();
            }

            if (getHidden || canInteractWith(sourceUser, user)) {
                return user;
            } else { // not looking for hidden and cannot interact (i.e is hidden)
                if (getOffline && user.getName().equalsIgnoreCase(searchTerm)) { // if looking for offline and got an exact match
                    return user;
                }
            }
            throw new PlayerNotFoundException();
        }
        final List<Player> matches = server.matchPlayer(searchTerm);

        if (matches.isEmpty()) {
            final String matchText = searchTerm.toLowerCase(Locale.ENGLISH);
            for (final User userMatch : getOnlineUsers()) {
                if (getHidden || canInteractWith(sourceUser, userMatch)) {
                    final String displayName = FormatUtil.stripFormat(userMatch.getDisplayName()).toLowerCase(Locale.ENGLISH);
                    if (displayName.contains(matchText)) {
                        return userMatch;
                    }
                }
            }
        } else {
            for (final Player player : matches) {
                final User userMatch = getUser(player);
                if (userMatch.getDisplayName().startsWith(searchTerm) && (getHidden || canInteractWith(sourceUser, userMatch))) {
                    return userMatch;
                }
            }
            final User userMatch = getUser(matches.get(0));
            if (getHidden || canInteractWith(sourceUser, userMatch)) {
                return userMatch;
            }
        }
        throw new PlayerNotFoundException();
    }

    @Override
    public boolean canInteractWith(final CommandSource interactor, final User interactee) {
        if (interactor == null) {
            return !interactee.isHidden();
        }

        if (interactor.isPlayer()) {
            return canInteractWith(getUser(interactor.getPlayer()), interactee);
        }

        return true; // console
    }

    @Override
    public boolean canInteractWith(final User interactor, final User interactee) {
        if (interactor == null) {
            return !interactee.isHidden();
        }

        if (interactor.equals(interactee)) {
            return true;
        }

        return interactor.getBase().canSee(interactee.getBase());
    }

    //This will create a new user if there is not a match.
    @Override
    public User getUser(final Player base) {
        if (base == null) {
            return null;
        }

        if (userMap == null) {
            LOGGER.log(Level.WARNING, "Essentials userMap not initialized");
            return null;
        }

        User user = userMap.getUser(base.getUniqueId());

        if (user == null) {
            if (getSettings().isDebug()) {
                LOGGER.log(Level.INFO, "Constructing new userfile from base player " + base.getName());
            }
            user = new User(base, this);
        } else {
            user.update(base);
        }
        return user;
    }


    @Override
    public World getWorld(final String name) {
        if (name.matches("[0-9]+")) {
            final int worldId = Integer.parseInt(name);
            if (worldId < getServer().getWorlds().size()) {
                return getServer().getWorlds().get(worldId);
            }
        }
        return getServer().getWorld(name);
    }

    @Override
    public BukkitTask runTaskAsynchronously(final Runnable run) {
        return this.getScheduler().runTaskAsynchronously(this, run);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(final Runnable run, final long delay) {
        return this.getScheduler().runTaskLaterAsynchronously(this, run, delay);
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(final Runnable run, final long delay, final long period) {
        return this.getScheduler().runTaskTimerAsynchronously(this, run, delay, period);
    }

    @Override
    public int scheduleSyncDelayedTask(final Runnable run) {
        return this.getScheduler().scheduleSyncDelayedTask(this, run);
    }

    @Override
    public int scheduleSyncDelayedTask(final Runnable run, final long delay) {
        return this.getScheduler().scheduleSyncDelayedTask(this, run, delay);
    }

    @Override
    public int scheduleSyncRepeatingTask(final Runnable run, final long delay, final long period) {
        return this.getScheduler().scheduleSyncRepeatingTask(this, run, delay, period);
    }

    @Override
    public PermissionsHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    @Override
    public AlternativeCommandsHandler getAlternativeCommandsHandler() {
        return alternativeCommandsHandler;
    }

    @Override
    public IItemDb getItemDb() {
        return itemDb;
    }

    @Override
    public UserMap getUserMap() {
        return userMap;
    }

    @Override
    public BalanceTop getBalanceTop() {
        return balanceTop;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    @Override
    public RedSmokesTimer getTimer() {
        return timer;
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return (Collection<Player>) getServer().getOnlinePlayers();
    }

    @Override
    public Iterable<User> getOnlineUsers() {
        final List<User> onlineUsers = new ArrayList<>();
        for (final Player player : getOnlinePlayers()) {
            onlineUsers.add(getUser(player));
        }
        return onlineUsers;
    }

    @Override
    public SpawnerItemProvider getSpawnerItemProvider() {
        return spawnerItemProvider;
    }

    @Override
    public SpawnerBlockProvider getSpawnerBlockProvider() {
        return spawnerBlockProvider;
    }

    @Override
    public SpawnEggProvider getSpawnEggProvider() {
        return spawnEggProvider;
    }

    @Override
    public PotionMetaProvider getPotionMetaProvider() {
        return potionMetaProvider;
    }

    @Override
    public CustomItemResolver getCustomItemResolver() {
        return customItemResolver;
    }

    @Override
    public ServerStateProvider getServerStateProvider() {
        return serverStateProvider;
    }

    public MaterialTagProvider getMaterialTagProvider() {
        return materialTagProvider;
    }

    @Override
    public ContainerProvider getContainerProvider() {
        return containerProvider;
    }

    @Override
    public KnownCommandsProvider getKnownCommandsProvider() {
        return knownCommandsProvider;
    }

    @Override
    public SerializationProvider getSerializationProvider() {
        return serializationProvider;
    }

    @Override
    public FormattedCommandAliasProvider getFormattedCommandAliasProvider() {
        return formattedCommandAliasProvider;
    }

    @Override
    public SyncCommandsProvider getSyncCommandsProvider() {
        return syncCommandsProvider;
    }

    @Override
    public PersistentDataProvider getPersistentDataProvider() {
        return persistentDataProvider;
    }

    @Override
    public ReflOnlineModeProvider getOnlineModeProvider() {
        return onlineModeProvider;
    }

    @Override
    public ItemUnbreakableProvider getItemUnbreakableProvider() {
        return unbreakableProvider;
    }

    @Override
    public WorldInfoProvider getWorldInfoProvider() {
        return worldInfoProvider;
    }

    @Override
    public SignDataProvider getSignDataProvider() {
        return signDataProvider;
    }

    @Override
    public PluginCommand getPluginCommand(final String cmd) {
        return this.getCommand(cmd);
    }

    private AbstractItemDb getItemDbFromConfig() {
        final String setting = settings.getItemDbType();

        if (setting.equalsIgnoreCase("json")) {
            return new FlatItemDb(this);
        } else if (setting.equalsIgnoreCase("csv")) {
            return new LegacyItemDb(this);
        } else {
            final VersionUtil.BukkitVersion version = VersionUtil.getServerBukkitVersion();

            if (version.isHigherThanOrEqualTo(VersionUtil.v1_13_0_R01)) {
                return new FlatItemDb(this);
            } else {
                return new LegacyItemDb(this);
            }
        }
    }

    private static class RedSmokesWorldListener implements Listener, Runnable {
        private transient final IRedSmokes redSmokes;

        RedSmokesWorldListener(final IRedSmokes redSmokes) {
            this.redSmokes = redSmokes;
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onWorldLoad(final WorldLoadEvent event) {
            PermissionsDefaults.registerBackDefaultFor(event.getWorld());
        }

        @Override
        public void run() {
            redSmokes.reload();
        }
    }
}
