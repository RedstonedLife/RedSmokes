package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.api.Economy;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.economy.vault.VaultEconomyProvider;
import com.bss.inc.redsmokes.main.items.AbstractItemDb;
import com.bss.inc.redsmokes.main.items.CustomItemResolver;
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
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import com.bss.inc.redsmokes.main.utils.logging.BaseLoggerProvider;
import io.papermc.lib.PaperLib;
import net.redsmokes.api.IConf;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.ISettings;
import net.redsmokes.api.commands.IrsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import sun.util.resources.cldr.ext.CurrencyNames_pa_Arab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private transient Map<String, IrsCommand> commandMap = new HashMap<>();

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
             * so users could register an email from the discord, like so /register-email <br>USERNAME</br> <br>PASSWORD</br> (In dms for safety)
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
                LOGGER.log(Level.INFO, "Essentials load " + timeroutput);
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
        super.onDisable();
    }



    private void handleCrash(final Throwable exception) {
        final PluginManager pm = getServer().getPluginManager();
        LOGGER.log(Level.SEVERE, exception.toString());
        exception.printStackTrace();
        pm.registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOW)
            public void onPlayerJoin(final PlayerJoinEvent event) {
                event.getPlayer().sendMessage("RedSmokes failed to load, read the log file");
            }
        }, this);
        //for(final Player player : getOn)
    }
}
