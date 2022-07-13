package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.provider.CommandSendListenerProvider;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import io.papermc.lib.PaperLib;
import net.redsmokes.api.events.AsyncUserDataLoadEvent;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.FormattedCommandAlias;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.bss.inc.redsmokes.main.I18n.tl;
import static com.earth2me.essentials.I18n.tl;

public class EssentialsPlayerListener implements Listener, FakeAccessor {
    private final transient IEssentials ess;
    private final ConcurrentHashMap<UUID, Integer> pendingMotdTasks = new ConcurrentHashMap<>();

    public EssentialsPlayerListener(final IEssentials parent) {
        this.ess = parent;
    }

    private static boolean isEntityPickupEvent() {
        try {
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isCommandSendEvent() {
        try {
            Class.forName("org.bukkit.event.player.PlayerCommandSendEvent");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isPaperCommandSendEvent() {
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isArrowPickupEvent() {
        try {
            Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    public void registerEvents() {
        ess.getServer().getPluginManager().registerEvents(this, ess);

        if (isArrowPickupEvent()) {
            ess.getServer().getPluginManager().registerEvents(new ArrowPickupListener(), ess);
        }

        if (isEntityPickupEvent()) {
            ess.getServer().getPluginManager().registerEvents(new PickupListener1_12(), ess);
        } else {
            ess.getServer().getPluginManager().registerEvents(new PickupListenerPre1_12(), ess);
        }

        if (isPaperCommandSendEvent()) {
            ess.getServer().getPluginManager().registerEvents(new PaperCommandSendListenerProvider(new CommandSendFilter()), ess);
        } else if (isCommandSendEvent()) {
            ess.getServer().getPluginManager().registerEvents(new BukkitCommandSendListenerProvider(new CommandSendFilter()), ess);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final User user = ess.getUser(event.getPlayer());
        updateCompass(user);
        user.setDisplayNick();

        if (ess.getSettings().isTeleportInvulnerability()) {
            user.enableInvulnerabilityAfterTeleport();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final User user = ess.getUser(event.getPlayer());
        if (user.isMuted()) {
            event.setCancelled(true);

            final String dateDiff = user.getMuteTimeout() > 0 ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
            if (dateDiff == null) {
                user.sendMessage(user.hasMuteReason() ? tl("voiceSilencedReason", user.getMuteReason()) : tl("voiceSilenced"));
            } else {
                user.sendMessage(user.hasMuteReason() ? tl("voiceSilencedReasonTime", dateDiff, user.getMuteReason()) : tl("voiceSilencedTime", dateDiff));
            }

            ess.getLogger().info(tl("mutedUserSpeaks", user.getName(), event.getMessage()));
        }
        try {
            final Iterator<Player> it = event.getRecipients().iterator();
            while (it.hasNext()) {
                final User u = ess.getUser(it.next());
                if (u.isIgnoredPlayer(user)) {
                    it.remove();
                }
            }
        } catch (final UnsupportedOperationException ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.INFO, "Ignore could not block chat due to custom chat plugin event.", ex);
            } else {
                ess.getLogger().info("Ignore could not block chat due to custom chat plugin event.");
            }
        }

        user.updateActivityOnChat(true);
        user.setDisplayNick();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        if (!ess.getSettings().cancelAfkOnMove() && !ess.getSettings().getFreezeAfkPlayers()) {
            event.getHandlers().unregister(this);

            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.INFO, "Unregistering move listener");
            }

            return;
        }

        final User user = ess.getUser(event.getPlayer());
        if (user.isAfk() && ess.getSettings().getFreezeAfkPlayers()) {
            final Location from = event.getFrom();
            final Location origTo = event.getTo();
            final Location to = origTo.clone();
            if (origTo.getY() >= from.getBlockY() + 1) {
                user.updateActivityOnMove(true);
                return;
            }
            to.setX(from.getX());
            to.setY(from.getY());
            to.setZ(from.getZ());
            try {
                if (event.getPlayer().getAllowFlight()) {
                    // Don't teleport to a safe location here, they are either a god or flying
                    throw new Exception();
                }
                event.setTo(LocationUtil.getSafeDestination(ess, to));
            } catch (final Exception ex) {
                event.setTo(to);
            }
            return;
        }
        final Location afk = user.getAfkPosition();
        if (afk == null || !event.getTo().getWorld().equals(afk.getWorld()) || afk.distanceSquared(event.getTo()) > 9) {
            user.updateActivityOnMove(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final User user = redSmokes.getUser(event.getPlayer());

        final Integer pendingId = pendingMotdTasks.remove(user.getUUID());
        if (pendingId != null) {
            redSmokes.getScheduler().cancelTask(pendingId);
        }
        user.startTransaction();
        if (user.isVanished()) {
            user.setVanished(false);
        }
        user.setLogoutLocation(user.getLocation());
        if (user.isRecipeSee()) {
            user.getBase().getOpenInventory().getTopInventory().clear();
        }
        if (!user.isHidden()) {
            user.setLastLogout(System.currentTimeMillis());
        }
        user.stopTransaction();
        user.dispose();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
    }

    public void delayedJoin(final Player player, final String message) {
        if (!player.isOnline()) {
            return;
        }

        redSmokes.getBackup().onPlayerJoin();
        final User dUser = redSmokes.getUser(player);

        dUser.startTransaction();
        if (dUser.isNPC()) {
            dUser.setNPC(false);
        }

        final long currentTime = System.currentTimeMillis();
        dUser.checkMuteTimeout(currentTime);
        dUser.updateActivity(false, AfkStatusChangeEvent.Cause.JOIN);
        dUser.stopTransaction();

        class DelayJoinTask implements Runnable {
            @Override
            public void run() {
                final User user = redSmokes.getUser(player);

                if (!user.getBase().isOnline()) {
                    return;
                }

                user.startTransaction();

                final String lastAccountName = user.getLastAccountName(); // For comparison
                user.setLastAccountName(user.getBase().getName());
                user.setLastLogin(currentTime);
                updateCompass(user);

                // Check for new username. If they don't want the message, let's just say it's false.
                final boolean newUsername = redSmokes.getSettings().isCustomNewUsernameMessage() && lastAccountName != null && !lastAccountName.equals(user.getBase().getName());
                redSmokes.runTaskAsynchronously(() -> redSmokes.getServer().getPluginManager().callEvent(new AsyncUserDataLoadEvent(user, effectiveMessage)));

                /** MAIL
                 *
                 */

                if (user.isAuthorized("redsmokes.updatecheck")) {
                    redSmokes.runTaskAsynchronously(() -> {
                        for (String str : redSmokes.getUpdateChecker().getVersionMessages(false, false)) {
                            user.sendMessage(str);
                        }
                    });
                }
                user.getConfirmingPayments().clear();
                user.stopTransaction();
            }
        }

        redSmokes.scheduleSyncDelayedTask(new DelayJoinTask());
    }

    // Makes the compass item ingame always point to the first essentials home.  #EasterEgg
    // EssentialsX: This can now optionally require a permission to enable, if set in the config.
    private void updateCompass(final User user) {
        if (redSmokes.getSettings().isCompassTowardsHomePerm() && !user.isAuthorized("essentials.home.compass")) return;

        final Location loc = user.getHome(user.getLocation());
        if (loc == null) {
            PaperLib.getBedSpawnLocationAsync(user.getBase(), false).thenAccept(location -> {
                if (location != null) {
                    user.getBase().setCompassTarget(location);
                }
            });
            return;
        }
        user.getBase().setCompassTarget(loc);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLoginBanned(final PlayerLoginEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (event.getResult() == Result.KICK_FULL) {
            final User kfuser = redSmokes.getUser(event.getPlayer());
            if (kfuser.isAuthorized("redsmokes.joinfullserver")) {
                event.allow();
                return;
            }
            if (redSmokes.getSettings().isCustomServerFullMessage()) {
                event.disallow(Result.KICK_FULL, tl("serverFull"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEggThrow(final PlayerEggThrowEvent event) {
        final User user = redSmokes.getUser(event.getPlayer());
        final ItemStack stack = new ItemStack(Material.EGG, 1);
        if (user.hasUnlimited(stack)) {
            user.getBase().getInventory().addItem(stack);
            user.getBase().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        final User user = redSmokes.getUser(event.getPlayer());
        if (user.hasUnlimited(new ItemStack(event.getBucket()))) {
            event.getItemStack().setType(event.getBucket());
            redSmokes.scheduleSyncDelayedTask(user.getBase()::updateInventory);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final String cmd = event.getMessage().toLowerCase(Locale.ENGLISH).split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
        final int argStartIndex = event.getMessage().indexOf(" ");
        final String args = argStartIndex == -1 ? "" // No arguments present
                : event.getMessage().substring(argStartIndex); // arguments start at argStartIndex; substring from there.

        // If the plugin command does not exist, check if it is an alias from commands.yml
        if (redSmokes.getServer().getPluginCommand(cmd) == null) {
            final Command knownCommand = redSmokes.getKnownCommandsProvider().getKnownCommands().get(cmd);
            if (knownCommand instanceof FormattedCommandAlias) {
                final FormattedCommandAlias command = (FormattedCommandAlias) knownCommand;
                for (String fullCommand : redSmokes.getFormattedCommandAliasProvider().createCommands(command, event.getPlayer(), args.split(" "))) {
                    handlePlayerCommandPreprocess(event, fullCommand);
                }
                return;
            }
        }

        // Handle the command given from the event.
        handlePlayerCommandPreprocess(event, cmd + args);
    }

    public void handlePlayerCommandPreprocess(final PlayerCommandPreprocessEvent event, final String effectiveCommand) {
        final Player player = event.getPlayer();
        final String cmd = effectiveCommand.toLowerCase(Locale.ENGLISH).split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
        final PluginCommand pluginCommand = redSmokes.getServer().getPluginCommand(cmd);

        final User user = redSmokes.getUser(player);

        if (redSmokes.getSettings().isCommandCooldownsEnabled()
                && !user.isAuthorized("redsmokes.commandcooldowns.bypass")) {
            final int argStartIndex = effectiveCommand.indexOf(" ");
            final String args = argStartIndex == -1 ? "" // No arguments present
                    : " " + effectiveCommand.substring(argStartIndex); // arguments start at argStartIndex; substring from there.
            final String fullCommand = pluginCommand == null ? effectiveCommand : pluginCommand.getName() + args;

            // Used to determine whether a user already has an existing cooldown
            // If so, no need to check for (and write) new ones.
            boolean cooldownFound = false;

            // Iterate over a copy of getCommandCooldowns in case of concurrent modifications
            for (final Entry<Pattern, Long> entry : new HashMap<>(user.getCommandCooldowns()).entrySet()) {
                // Remove any expired cooldowns
                if (entry.getValue() <= System.currentTimeMillis()) {
                    user.clearCommandCooldown(entry.getKey());
                    // Don't break in case there are other command cooldowns left to clear.
                } else if (entry.getKey().matcher(fullCommand).matches()) {
                    // User's current cooldown hasn't expired, inform and terminate cooldown code.
                    if (entry.getValue() > System.currentTimeMillis()) {
                        final String commandCooldownTime = DateUtil.formatDateDiff(entry.getValue());
                        user.sendMessage(tl("commandCooldown", commandCooldownTime));
                        cooldownFound = true;
                        event.setCancelled(true);
                        break;
                    }
                }
            }

            if (!cooldownFound) {
                final Entry<Pattern, Long> cooldownEntry = ess.getSettings().getCommandCooldownEntry(fullCommand);

                if (cooldownEntry != null) {
                    if (ess.getSettings().isDebug()) {
                        ess.getLogger().info("Applying " + cooldownEntry.getValue() + "ms cooldown on /" + fullCommand + " for" + user.getName() + ".");
                    }
                    final Date expiry = new Date(System.currentTimeMillis() + cooldownEntry.getValue());
                    user.addCommandCooldown(cooldownEntry.getKey(), expiry, ess.getSettings().isCommandCooldownPersistent(fullCommand));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorldFlyReset(final PlayerChangedWorldEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final User user = redSmokes.getUser(event.getPlayer());
        final String newWorld = event.getPlayer().getLocation().getWorld().getName();

        if (!user.getWorld().getName().equals(newWorld)) {
            user.sendMessage(tl("currentWorld", newWorld));
        }
        if (user.isVanished()) {
            user.setVanished(user.isAuthorized("redsmokes.vanish"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        boolean updateActivity = true;

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                break;
            case LEFT_CLICK_AIR:
                break;
            case LEFT_CLICK_BLOCK:
                break;
            case PHYSICAL:
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClickEvent(final InventoryClickEvent event) {
        Player refreshPlayer = null;
        final Inventory top = event.getView().getTopInventory();
        final InventoryType type = top.getType();

        final Inventory clickedInventory;
        if (event.getRawSlot() < 0) {
            clickedInventory = null;
        } else {
            clickedInventory = event.getRawSlot() < top.getSize() ? top : event.getView().getBottomInventory();
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseEvent(final InventoryCloseEvent event) {
    }


    private final class PickupListenerPre1_12 implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPlayerPickupItem(final org.bukkit.event.player.PlayerPickupItemEvent event) {
        }
    }

    private final class PickupListener1_12 implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPlayerPickupItem(final org.bukkit.event.entity.EntityPickupItemEvent event) {
        }
    }

    private final class CommandSendFilter implements CommandSendListenerProvider.Filter {
        @Override
        public Predicate<String> apply(Player player) {
            final User user = redSmokes.getUser(player);
            final Set<PluginCommand> checked = new HashSet<>();
            final Set<PluginCommand> toRemove = new HashSet<>();

            return label -> {
                if (isEssentialsCommand(label)) {
                    final PluginCommand command = redSmokes.getServer().getPluginCommand(label);
                    if (!checked.contains(command)) {
                        checked.add(command);
                        if (!user.isAuthorized(command.getName().equals("r") ? "redsmokes.msg" : "redsmokes." + command.getName())) {
                            toRemove.add(command);
                        }
                    }
                    return toRemove.contains(command);
                }
                return false;
            };
        }

        /**
         * Returns true if all of the following are true:
         * - The command is a plugin command
         * - The plugin command is from an official EssentialsX plugin or addon
         * - There is no known alternative OR the alternative is overridden by Essentials
         */
        private boolean isEssentialsCommand(final String label) {
            final PluginCommand command = redSmokes.getServer().getPluginCommand(label);

            return command != null
                    && (command.getPlugin() == ess || command.getPlugin().getClass().getName().startsWith("com.earth2me.essentials") || command.getPlugin().getClass().getName().startsWith("net.essentialsx"))
                    && (redSmokes.getSettings().isCommandOverridden(label) || (redSmokes.getAlternativeCommandsHandler().getAlternative(label) == null));
        }
    }

    @Override
    public void getUser(Player player) {
        redSmokes.getUser(player);
    }
}

