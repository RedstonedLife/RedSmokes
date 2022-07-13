package com.bss.inc.redsmokes.main;

import net.redsmokes.api.commands.IrsCommand;
import net.redsmokes.api.services.BalanceTop;
import com.bss.inc.redsmokes.main.api.IItemDb;
import com.bss.inc.redsmokes.main.commands.PlayerNotFoundException;
import com.bss.inc.redsmokes.main.nms.refl.providers.ReflOnlineModeProvider;
import com.bss.inc.redsmokes.main.perm.PermissionsHandler;
import com.bss.inc.redsmokes.main.provider.SpawnerItemProvider;
import com.bss.inc.redsmokes.main.provider.SpawnerBlockProvider;
import com.bss.inc.redsmokes.main.provider.ServerStateProvider;
import com.bss.inc.redsmokes.main.provider.MaterialTagProvider;
import com.bss.inc.redsmokes.main.provider.ContainerProvider;
import com.bss.inc.redsmokes.main.provider.KnownCommandsProvider;
import com.bss.inc.redsmokes.main.provider.SerializationProvider;
import com.bss.inc.redsmokes.main.provider.FormattedCommandAliasProvider;
import com.bss.inc.redsmokes.main.provider.SyncCommandsProvider;
import com.bss.inc.redsmokes.main.provider.PersistentDataProvider;
import com.bss.inc.redsmokes.main.provider.ItemUnbreakableProvider;
import com.bss.inc.redsmokes.main.provider.WorldInfoProvider;
import com.bss.inc.redsmokes.main.provider.SignDataProvider;
import com.bss.inc.redsmokes.main.updatecheck.UpdateChecker;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public interface IRedSmokes extends Plugin {
    void reload();
    Map<String, IrsCommand> getCommandMap();
    List<String> onTabCompleteRedSmokes(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IRedSmokesModule module);
    boolean onCommandRedSmokes(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IRedSmokesModule module);
    @Deprecated
    User getUser(Object base);
    User getUser(UUID base);
    User getUser(String base);
    User getUser(Player base);
    User matchUser(Server server, User sourceUser, String searchTerm, Boolean getHidden, boolean getOffline) throws PlayerNotFoundException;
    boolean canInteractWith(CommandSource interactor, User interactee);
    boolean canInteractWith(User interactor, User interactee);
    I18n getI18n();
    User getOfflineUser(String name);
    World getWorld(String name);
    ISettings getSettings();
    BukkitScheduler getScheduler();
    Backup getBackup();
    UpdateChecker getUpdateChecker();
    BukkitTask runTaskAsynchronously(Runnable run);
    Worth getWorth();
    BukkitTask runTaskLaterAsynchronously(Runnable run, long delay);
    BukkitTask runTaskTimerAsynchronously(Runnable run, long delay, long period);
    int scheduleSyncDelayedTask(Runnable run);
    int scheduleSyncDelayedTask(Runnable run, long delay);
    int scheduleSyncRepeatingTask(Runnable run, long delay, long period);
    PermissionsHandler getPermissionsHandler();
    AlternativeCommandsHandler getAlternativeCommandsHandler();
    void showError(CommandSource sender, Throwable exception, String commandLabel);
    IItemDb getItemDb();
    UserMap getUserMap();
    BalanceTop getBalanceTop();
    RedSmokesTimer getTimer();
    //MailService getMail();
    /**
     * Get a list of players who are vanished.
     *
     * @return A list of players who are vanished
     * @deprecated Use {@link net.redsmokes.api.IRedSmokes#getVanishedPlayersNew()} where possible.
     */
    Collection<Player> getOnlinePlayers();
    Iterable<User> getOnlineUsers();
    SpawnerItemProvider getSpawnerItemProvider();

    SpawnerBlockProvider getSpawnerBlockProvider();

    ServerStateProvider getServerStateProvider();

    MaterialTagProvider getMaterialTagProvider();

    ContainerProvider getContainerProvider();

    KnownCommandsProvider getKnownCommandsProvider();

    SerializationProvider getSerializationProvider();

    FormattedCommandAliasProvider getFormattedCommandAliasProvider();

    SyncCommandsProvider getSyncCommandsProvider();

    PersistentDataProvider getPersistentDataProvider();

    ReflOnlineModeProvider getOnlineModeProvider();

    ItemUnbreakableProvider getItemUnbreakableProvider();

    WorldInfoProvider getWorldInfoProvider();

    SignDataProvider getSignDataProvider();
    PluginCommand getPluginCommand(String cmd);
}
