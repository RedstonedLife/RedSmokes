package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.items.AbstractItemDb;
import com.bss.inc.redsmokes.main.items.CustomItemResolver;
import com.bss.inc.redsmokes.main.metrics.MetricsWrapper;
import com.bss.inc.redsmokes.main.nms.refl.providers.ReflOnlineModeProvider;
import com.bss.inc.redsmokes.main.perm.PermissionsHandler;
import com.bss.inc.redsmokes.main.provider.*;
import com.bss.inc.redsmokes.main.updatecheck.UpdateChecker;
import net.redsmokes.api.IConf;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.ISettings;
import net.redsmokes.api.commands.IrsCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Override
    public void onEnable() {
        try {
            if(BUKKIT_LOGGER != super.getLogger()) {
                BUKKIT_LOGGER.setParent(super.getLogger());
            }
            LOGGER = RedSmokesLogger.getLoggerProvider(this);
            RedSmokesLogger.updatePluginLogger(this);
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

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public <T> RegisteredServiceProvider<T> getServiceProvider(Class<T> clazz) {
        return Bukkit.getServicesManager().getRegistration(clazz);
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
