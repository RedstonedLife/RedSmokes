package com.bss.inc.redsmokes.main.commands;

import com.bss.inc.redsmokes.main.CommandSource;
import com.bss.inc.redsmokes.main.economy.EconomyLayer;
import com.bss.inc.redsmokes.main.economy.EconomyLayers;
import com.bss.inc.redsmokes.main.utils.EnumUtil;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Commandredsmokes extends RedSmokesCommand {

    private static final Sound NOTE_HARP = EnumUtil.valueOf(Sound.class, "BLOCK_NOTE_BLOCK_HARP", "BLOCK_NOTE_HARP", "NOTE_PIANO");
    private static final Sound MOO_SOUND = EnumUtil.valueOf(Sound.class, "COW_IDLE", "ENTITY_COW_MILK");

    private static final String NYAN_TUNE = "1D#,1E,2F#,,2A#,1E,1D#,1E,2F#,2B,2D#,2E,2D#,2A#,2B,,2F#,,1D#,1E,2F#,2B,2C#,2A#,2B,2C#,2E,2D#,2E,2C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1B,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1B,,";
    private static final String[] CONSOLE_MOO = new String[] {"         (__)", "         (oo)", "   /------\\/", "  / |    ||", " *  /\\---/\\", "    ~~   ~~", "....\"Have you mooed today?\"..."};
    private static final String[] PLAYER_MOO = new String[] {"            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..."};
    private static final List<String> versionPlugins = Arrays.asList(
            "Vault", // API
            "Reserve", // API
            "PlaceholderAPI", // API
            "CMI", // potential for issues
            "Towny", // past issues; admins should ensure latest
            "ChestShop", // past issues; admins should ensure latest
            "Citizens", // fires player events
            "LuckPerms", // permissions (recommended)
            "UltraPermissions",
            "PermissionsEx", // permissions (unsupported)
            "GroupManager", // permissions (unsupported)
            "bPermissions", // permissions (unsupported)
            "DiscordSRV" // potential for issues if EssentialsXDiscord is installed
    );
    private static final List<String> warnPlugins = Arrays.asList(
            "PermissionsEx",
            "GroupManager",
            "bPermissions"
    );
    private transient TuneRunnable currentTune = null;

    public Commandredsmokes() {
        super("redsmokes");
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if(args.length == 0) {showUsage(sender);}
        switch (args[0]) {
            // Info commands
            case "debug":
            case "verbose":
                runDebug(server, sender, commandLabel, args);
                break;
            case "ver":
            case "version":
                runVersion(server, sender, commandLabel, args);
                break;
            case "reload":
                runReload(server, sender, commandLabel, args);
                break;
            case "nya":
            case "nyan":
                runNya(server, sender, commandLabel, args);
                break;
            case "moo":
                runMoo(server, sender, commandLabel, args);
                break;
            default:
                showUsage(sender);
                break;
        }
    }

    private void showUsage(final CommandSource sender) throws Exception {
        throw new NotEnoughArgumentsException();
    }

    // Toggles debug mode.
    private void runDebug(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        redSmokes.getSettings().setDebug(!redSmokes.getSettings().isDebug());
        sender.sendMessage("RedSmokes " + redSmokes.getDescription().getVersion() + " debug mode " + (redSmokes.getSettings().isDebug() ? "enabled" : "disabled"));
    }

    // Reloads all reloadable configs.
    private void runReload(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        redSmokes.reload();
        sender.sendMessage(tl("redsmokesReload", redSmokes.getDescription().getVersion()));
    }

    // Pop tarts.
    private void runNya(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (currentTune != null) {
            currentTune.cancel();
        }

        currentTune = new TuneRunnable(NYAN_TUNE, NOTE_HARP, redSmokes::getOnlinePlayers);
        currentTune.runTaskTimer(redSmokes, 20, 2);
    }

    // Cow farts.
    private void runMoo(final Server server, final CommandSource sender, final String command, final String[] args) {
        if (args.length == 2 && args[1].equals("moo")) {
            for (final String s : CONSOLE_MOO) {
                redSmokes.getLogger().info(s);
            }
            for (final Player player : redSmokes.getOnlinePlayers()) {
                player.sendMessage(PLAYER_MOO);
                player.playSound(player.getLocation(), MOO_SOUND, 1, 1.0f);
            }
        } else {
            if (sender.isPlayer()) {
                sender.getSender().sendMessage(PLAYER_MOO);
                final Player player = sender.getPlayer();
                player.playSound(player.getLocation(), MOO_SOUND, 1, 1.0f);

            } else {
                sender.getSender().sendMessage(CONSOLE_MOO);
            }
        }
    }

    // Displays versions of EssentialsX and related plugins.
    private void runVersion(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (sender.isPlayer() && !redSmokes.getUser(sender.getPlayer()).isAuthorized("redsmokes.version")) return;

        boolean isMismatched = false;
        boolean isVaultInstalled = false;
        boolean isUnsupported = false;
        final VersionUtil.SupportStatus supportStatus = VersionUtil.getServerSupportStatus();
        final PluginManager pm = server.getPluginManager();
        final String essVer = pm.getPlugin("RedSmokes").getDescription().getVersion();

        final String serverMessageKey;
        if (supportStatus.isSupported()) {
            serverMessageKey = "versionOutputFine";
        } else if (supportStatus == VersionUtil.SupportStatus.UNSTABLE) {
            serverMessageKey = "versionOutputUnsupported";
        } else {
            serverMessageKey = "versionOutputWarn";
        }

        sender.sendMessage(tl(serverMessageKey, "Server", server.getBukkitVersion() + " " + server.getVersion()));
        sender.sendMessage(tl(serverMessageKey, "Brand", server.getName()));
        sender.sendMessage(tl("versionOutputFine", "RedSmokes", essVer));

        for (final Plugin plugin : pm.getPlugins()) {
            final PluginDescriptionFile desc = plugin.getDescription();
            String name = desc.getName();
            final String version = desc.getVersion();

            if (versionPlugins.contains(name)) {
                if (warnPlugins.contains(name)) {
                    sender.sendMessage(tl("versionOutputUnsupported", name, version));
                    isUnsupported = true;
                } else {
                    sender.sendMessage(tl("versionOutputFine", name, version));
                }
            }

            if (name.equals("Vault")) isVaultInstalled = true;
        }

        final String layer;
        if (redSmokes.getSettings().isEcoDisabled()) {
            layer = "Disabled";
        } else if (EconomyLayers.isLayerSelected()) {
            final EconomyLayer economyLayer = EconomyLayers.getSelectedLayer();
            layer = economyLayer.getName() + " (" + economyLayer.getBackendName() + ")";
        } else {
            layer = "None";
        }
        sender.sendMessage(tl("versionOutputEconLayer", layer));

        if (isMismatched) {
            sender.sendMessage(tl("versionMismatchAll"));
        }

        if (!isVaultInstalled) {
            sender.sendMessage(tl("versionOutputVaultMissing"));
        }

        if (isUnsupported) {
            sender.sendMessage(tl("versionOutputUnsupportedPlugins"));
        }

        switch (supportStatus) {
            case NMS_CLEANROOM:
                sender.sendMessage(ChatColor.DARK_RED + tl("serverUnsupportedCleanroom"));
                break;
            case DANGEROUS_FORK:
                sender.sendMessage(ChatColor.DARK_RED + tl("serverUnsupportedDangerous"));
                break;
            case UNSTABLE:
                sender.sendMessage(ChatColor.DARK_RED + tl("serverUnsupportedMods"));
                break;
            case OUTDATED:
                sender.sendMessage(ChatColor.RED + tl("serverUnsupported"));
                break;
            case LIMITED:
                sender.sendMessage(ChatColor.RED + tl("serverUnsupportedLimitedApi"));
                break;
        }
        if (VersionUtil.getSupportStatusClass() != null) {
            sender.sendMessage(ChatColor.RED + tl("serverUnsupportedClass", VersionUtil.getSupportStatusClass()));
        }

        sender.sendMessage(tl("versionFetching"));
        redSmokes.runTaskAsynchronously(() -> {
            for (String str : redSmokes.getUpdateChecker().getVersionMessages(true, true)) {
                sender.sendMessage(str);
            }
        });
    }

    private static class TuneRunnable extends BukkitRunnable {
        private static final Map<String, Float> noteMap = ImmutableMap.<String, Float>builder()
                .put("1F#", 0.5f)
                .put("1G", 0.53f)
                .put("1G#", 0.56f)
                .put("1A", 0.6f)
                .put("1A#", 0.63f)
                .put("1B", 0.67f)
                .put("1C", 0.7f)
                .put("1C#", 0.76f)
                .put("1D", 0.8f)
                .put("1D#", 0.84f)
                .put("1E", 0.9f)
                .put("1F", 0.94f)
                .put("2F#", 1.0f)
                .put("2G", 1.06f)
                .put("2G#", 1.12f)
                .put("2A", 1.18f)
                .put("2A#", 1.26f)
                .put("2B", 1.34f)
                .put("2C", 1.42f)
                .put("2C#", 1.5f)
                .put("2D", 1.6f)
                .put("2D#", 1.68f)
                .put("2E", 1.78f)
                .put("2F", 1.88f)
                .build();

        private final String[] tune;
        private final Sound sound;
        private final Supplier<Collection<Player>> players;
        private int i = 0;

        TuneRunnable(final String tuneStr, final Sound sound, final Supplier<Collection<Player>> players) {
            this.tune = tuneStr.split(",");
            this.sound = sound;
            this.players = players;
        }

        @Override
        public void run() {
            final String note = tune[i];
            i++;
            if (i >= tune.length) {
                cancel();
            }
            if (note == null || note.isEmpty()) {
                return;
            }

            for (final Player onlinePlayer : players.get()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), sound, 1, noteMap.get(note));
            }
        }
    }

}
