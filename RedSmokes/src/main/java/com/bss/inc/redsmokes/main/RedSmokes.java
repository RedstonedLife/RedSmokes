package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.api.Economy;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.economy.vault.VaultEconomyProvider;
import com.bss.inc.redsmokes.main.items.AbstractItemDb;
import com.bss.inc.redsmokes.main.items.CustomItemResolver;
import com.bss.inc.redsmokes.main.metrics.MetricsWrapper;
import com.bss.inc.redsmokes.main.nms.refl.providers.ReflOnlineModeProvider;
import com.bss.inc.redsmokes.main.perm.PermissionsHandler;
import com.bss.inc.redsmokes.main.provider.*;
import com.bss.inc.redsmokes.main.updatecheck.UpdateChecker;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import com.bss.inc.redsmokes.main.utils.logging.BaseLoggerProvider;
import net.redsmokes.api.IConf;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.ISettings;
import net.redsmokes.api.commands.IrsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

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
