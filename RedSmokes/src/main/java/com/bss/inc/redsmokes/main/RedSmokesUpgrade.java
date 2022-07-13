package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.FakeWorld;
import com.bss.inc.redsmokes.main.config.ConfigurateUtil;
import com.bss.inc.redsmokes.main.config.RedSmokesConfiguration;
import com.bss.inc.redsmokes.main.config.RedSmokesUserConfiguration;
import com.bss.inc.redsmokes.main.utils.StringUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.services.mail.MailMessage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bss.inc.redsmokes.main.I18n.tl;


public class RedSmokesUpgrade {
    private static final FileFilter YML_FILTER = pathname -> pathname.isFile() && pathname.getName().endsWith(".yml");
    private static final String PATTERN_CONFIG_UUID_REGEX = "(?mi)^uuid:\\s*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\s*$";
    private static final Pattern PATTERN_CONFIG_UUID = Pattern.compile(PATTERN_CONFIG_UUID_REGEX);
    private static final String PATTERN_CONFIG_NAME_REGEX = "(?mi)^lastAccountName:\\s*[\"']?(\\w+)[\"']?\\s*$";
    private static final Pattern PATTERN_CONFIG_NAME = Pattern.compile(PATTERN_CONFIG_NAME_REGEX);
    private final transient IRedSmokes redSmokes;
    private final transient RedSmokesConfiguration doneFile;

    RedSmokesUpgrade(final IRedSmokes provRedSmokes) {
        redSmokes = provRedSmokes;
        if (!redSmokes.getDataFolder().exists()) {
            redSmokes.getDataFolder().mkdirs();
        }
        doneFile = new RedSmokesConfiguration(new File(redSmokes.getDataFolder(), "upgrades-done.yml"));
        doneFile.load();
    }

    public static void uuidFileConvert(final IRedSmokes redSmokes, final Boolean ignoreUFCache) {
        redSmokes.getLogger().info("Starting Essentials UUID userdata conversion");

        final File userdir = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdir.exists()) {
            return;
        }

        int countFiles = 0;
        int countFails = 0;
        int countEssCache = 0;
        int countBukkit = 0;

        redSmokes.getLogger().info("Found " + userdir.list().length + " files to convert...");

        for (final String string : userdir.list()) {
            if (!string.endsWith(".yml") || string.length() < 5) {
                continue;
            }

            final int showProgress = countFiles % 250;

            if (showProgress == 0) {
                redSmokes.getUserMap().getUUIDMap().forceWriteUUIDMap();
                redSmokes.getLogger().info("Converted " + countFiles + "/" + userdir.list().length);
            }

            countFiles++;

            final String name = string.substring(0, string.length() - 4);
            final RedSmokesUserConfiguration config;
            UUID uuid = null;
            try {
                uuid = UUID.fromString(name);
            } catch (final IllegalArgumentException ex) {
                final File file = new File(userdir, string);
                final RedSmokesConfiguration conf = new RedSmokesConfiguration(file);
                conf.load();
                conf.setProperty("lastAccountName", name);
                conf.save();

                final String uuidConf = ignoreUFCache ? "force-uuid" : "uuid";

                final String uuidString = conf.getString(uuidConf, null);

                for (int i = 0; i < 4; i++) {
                    try {
                        uuid = UUID.fromString(uuidString);
                        countEssCache++;
                        break;
                    } catch (final Exception ex2) {
                        if (conf.getBoolean("npc", false)) {
                            uuid = UUID.nameUUIDFromBytes(("NPC:" + name).getBytes(Charsets.UTF_8));
                            break;
                        }

                        final org.bukkit.OfflinePlayer player = redSmokes.getServer().getOfflinePlayer(name);
                        uuid = player.getUniqueId();
                    }

                    if (uuid != null) {
                        countBukkit++;
                        break;
                    }
                }

                if (uuid != null) {
                    conf.blockingSave();
                    config = new RedSmokesUserConfiguration(name, uuid, new File(userdir, uuid + ".yml"));
                    config.convertLegacyFile();
                    redSmokes.getUserMap().trackUUID(uuid, name, false);
                    continue;
                }
                countFails++;
            }
        }
        redSmokes.getUserMap().getUUIDMap().forceWriteUUIDMap();

        redSmokes.getLogger().info("Converted " + countFiles + "/" + countFiles + ".  Conversion complete.");
        redSmokes.getLogger().info("Converted via cache: " + countEssCache + " :: Converted via lookup: " + countBukkit + " :: Failed to convert: " + countFails);
        redSmokes.getLogger().info("To rerun the conversion type /essentials uuidconvert");
    }

    public void convertStupidCamelCaseUserdataKeys() {
        if (doneFile.getBoolean("updateUsersStupidLegacyPathNames", false)) {
            return;
        }

        redSmokes.getLogger().info("Attempting to migrate legacy userdata keys to Configurate");

        final File userdataFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            return;
        }
        final File[] userFiles = userdataFolder.listFiles();

        for (final File file : userFiles) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) {
                continue;
            }
            final RedSmokesConfiguration config = new RedSmokesConfiguration(file);
            try {
                config.load();

                if (config.hasProperty("muteReason")) {
                    final String reason = config.getString("muteReason", null);
                    config.removeProperty("muteReason");
                    config.setProperty("mute-reason", reason);
                }

                if (config.hasProperty("ipAddress")) {
                    final String ip = config.getString("ipAddress", null);
                    config.removeProperty("ipAddress");
                    config.setProperty("ip-address", ip);
                }

                if (config.hasProperty("lastAccountName")) {
                    final String name = config.getString("lastAccountName", null);
                    config.removeProperty("lastAccountName");
                    config.setProperty("last-account-name", name);
                }

                if (config.hasProperty("acceptingPay")) {
                    final boolean isPay = config.getBoolean("acceptingPay", true);
                    config.removeProperty("acceptingPay");
                    config.setProperty("accepting-pay", isPay);
                }
                config.blockingSave();
            } catch (final RuntimeException ex) {
                redSmokes.getLogger().log(Level.INFO, "File: " + file);
                throw ex;
            }
        }
        doneFile.setProperty("updateUsersStupidLegacyPathNames", true);
        doneFile.save();
        redSmokes.getLogger().info("Done converting legacy userdata keys to Configurate.");
    }

    /**
     * This migration cleans up unused files left behind by the chaos resulting from Vault's questionable economy
     * integration, and upstream Essentials' rushed and untested 1.7.10 UUID support.
     * Both of these have been fixed in EssentialsX as of 2.18.x and 2.19.x respectively, but the leftover userdata
     * files can reach into the tens of thousands and can cause excessive memory and storage usage, so this migration
     * relocates these files to a backup folder to be removed by the server owner at a later date.
     * <p>
     * To quote JRoy, who suffered immensely while trying to debug and fix various related issues:
     * <p>
     * "RedSmokes decided when adding its initial support for UUIDs, it wanted an implementation which would cause
     * eternal pain and suffering for any person who dared touch any of the code in the future. This code that was made
     * was so bad, it managed to somehow not maintain any actual UUID support for any external integrations/plugins.
     * Up until 2.19.0 and 2.18.0 respectively, our Vault integration and entire Economy system was entirely based off
     * username strings, and thanks to Vault being a flawed standard, for some reason exposes account create to third
     * party plugins rather than letting the implementation handle it. That doesn't seem like a huge problem at the
     * surface, but there was one small problem: whoever made the Vault integration for RedSmokes suffered a stroke in
     * the process of creating it. The implementation for the createAccount method, regardless of whether it was an
     * actual player or an NPC account (which the Vault spec NEVER accounted for but plugins just have to guess when
     * to support them), it would always create an NPC account. This caused any plugin integrating with Vault, creating
     * NPC accounts for pretty much every single player on the server. It still, to this day, amazes me how nobody saw
     * this code and didn't die without rewriting it; Or how everybody simply didn't stop using this plugin because how
     * awful that godforsaken code was. Anyways, this upgrade does its best to delete NPC accounts created by the
     * horrible economy code, as any operation which loads all user data into memory will load all these NPC accounts
     * and spam the console with warnings."
     */
    public void purgeBrokenNpcAccounts() {
        if (doneFile.getBoolean("updatePurgeBrokenNpcAccounts", false)) {
            return;
        }

        final File userdataFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            return;
        }
        final File[] userFiles = userdataFolder.listFiles();
        if (userFiles.length == 0) {
            return;
        }
        final File backupFolder = new File(redSmokes.getDataFolder(), "userdata-npc-backup");
        if (backupFolder.exists()) {
            redSmokes.getLogger().info("NPC backup folder already exists; skipping NPC purge.");
            redSmokes.getLogger().info("To finish purging broken NPC accounts, rename the \"plugins/RedSmokes/userdata-npc-backup\" folder and restart your server.");
            return;
        } else if (!backupFolder.mkdir()) {
            redSmokes.getLogger().info("Skipping NPC purge due to error creating backup folder.");
            return;
        }

        redSmokes.getLogger().info("#===========================================================================#");
        redSmokes.getLogger().info(" RedSmokes will now purge any NPC accounts which were incorrectly created.");
        redSmokes.getLogger().info(" Only NPC accounts with the default starting balance will be deleted. If");
        redSmokes.getLogger().info(" they turn out to be valid NPC accounts, they will be re-created as needed.");
        redSmokes.getLogger().info(" Any files deleted here will be backed up to the ");
        redSmokes.getLogger().info(" \"plugins/RedSmokes/userdata-npc-backup\" folder. If you notice any files");
        redSmokes.getLogger().info(" have been purged incorrectly, you should restore it from the backup and");
        redSmokes.getLogger().info(" report it to us on GitHub:");
        redSmokes.getLogger().info(" https://github.com/RedstonedLife/RedSmokes/issues/new/choose");
        redSmokes.getLogger().info("");
        redSmokes.getLogger().info(" NOTE: This is a one-time process and will take several minutes if you have");
        redSmokes.getLogger().info(" a lot of userdata files! If you interrupt this process, RedSmokes will");
        redSmokes.getLogger().info(" skip the process until you rename or remove the backup folder.");
        redSmokes.getLogger().info("#===========================================================================#");

        final int totalUserFiles = userFiles.length;
        redSmokes.getLogger().info("Found ~" + totalUserFiles + " files under \"plugins/RedSmokes/userdata\"...");

        final AtomicInteger movedAccounts = new AtomicInteger(0);
        final AtomicInteger totalAccounts = new AtomicInteger(0);

        // Less spammy feedback for greater userdata counts, eg 100 files -> 5 seconds, 1k -> 7s, 10k -> 9s, 100k -> 11s, 1m -> 14s
        final long feedbackInterval = Math.min(15, 1 + Math.round(2.1 * Math.log10(userFiles.length)));

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final ScheduledFuture<?> feedbackTask = executor.scheduleWithFixedDelay(
                () -> redSmokes.getLogger().info("Scanned " + totalAccounts.get() + "/" + totalUserFiles + " accounts; moved " + movedAccounts.get() + " accounts"),
                5, feedbackInterval, TimeUnit.SECONDS);

        for (final File file : userFiles) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) {
                continue;
            }
            final RedSmokesConfiguration config = new RedSmokesConfiguration(file);
            try {
                totalAccounts.incrementAndGet();
                config.load();

                if (config.getKeys().size() > 4) {
                    continue;
                }

                if (!config.getBoolean("npc", false)) {
                    continue;
                }

                final BigDecimal money = config.getBigDecimal("money", null);
                if (money == null || money.compareTo(redSmokes.getSettings().getStartingBalance()) != 0) {
                    continue;
                }

                if (config.getKeys().size() == 4 && !config.hasProperty("last-account-name") && config.hasProperty("mail")) {
                    continue;
                }

                try {
                    //noinspection UnstableApiUsage
                    Files.move(file, new File(backupFolder, file.getName()));
                    movedAccounts.incrementAndGet();
                } catch (IOException e) {
                    redSmokes.getLogger().log(Level.SEVERE, "Error while moving NPC file", e);
                }
            } catch (final RuntimeException ex) {
                redSmokes.getLogger().log(Level.INFO, "File: " + file);
                feedbackTask.cancel(false);
                executor.shutdown();
                throw ex;
            }
        }
        feedbackTask.cancel(false);
        executor.shutdown();
        doneFile.setProperty("updatePurgeBrokenNpcAccounts", true);
        doneFile.save();

        redSmokes.getLogger().info("#===========================================================================#");
        redSmokes.getLogger().info(" RedSmokes has finished purging NPC accounts.");
        redSmokes.getLogger().info("");
        redSmokes.getLogger().info(" Deleted accounts: " + movedAccounts);
        redSmokes.getLogger().info(" Total accounts processed: " + totalAccounts);
        redSmokes.getLogger().info("");
        redSmokes.getLogger().info(" Purged accounts have been backed up to");
        redSmokes.getLogger().info(" \"plugins/RedSmokes/userdata-npc-backup\", and can be restored from there");
        redSmokes.getLogger().info(" if needed. Please report any files which have been incorrectly deleted");
        redSmokes.getLogger().info(" to us on GitHub:");
        redSmokes.getLogger().info(" https://github.com/RedstonedLife/RedSmokes/issues/new/choose");
        redSmokes.getLogger().info("#===========================================================================#");
    }

    public void convertIgnoreList() {
        final Pattern pattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        if (doneFile.getBoolean("updateUsersIgnoreListUUID", false)) {
            return;
        }

        redSmokes.getLogger().info("Attempting to migrate ignore list to UUIDs");

        final File userdataFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            return;
        }
        final File[] userFiles = userdataFolder.listFiles();

        for (final File file : userFiles) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) {
                continue;
            }
            final RedSmokesConfiguration config = new RedSmokesConfiguration(file);
            try {
                config.load();
                if (config.hasProperty("ignore")) {
                    final List<String> migratedIgnores = new ArrayList<>();
                    for (final String name : Collections.synchronizedList(config.getList("ignore", String.class))) {
                        if (name == null) {
                            continue;
                        }
                        if (pattern.matcher(name.trim()).matches()) {
                            redSmokes.getLogger().info("Detected already migrated ignore list!");
                            return;
                        }
                        final User user = redSmokes.getOfflineUser(name);
                        if (user != null && user.getBase() != null) {
                            migratedIgnores.add(user.getBase().getUniqueId().toString());
                        }
                    }
                    config.removeProperty("ignore");
                    config.setProperty("ignore", migratedIgnores);
                    config.blockingSave();
                }
            } catch (final RuntimeException ex) {
                redSmokes.getLogger().log(Level.INFO, "File: " + file);
                throw ex;
            }
        }
        doneFile.setProperty("updateUsersIgnoreListUUID", true);
        doneFile.save();
        redSmokes.getLogger().info("Done converting ignore list.");
    }

    private void removeLinesFromConfig(final File file, final String regex, final String info) throws Exception {
        boolean needUpdate = false;
        final BufferedReader bReader = new BufferedReader(new FileReader(file));
        final File tempFile = File.createTempFile("redsmokesupgrade", ".tmp.yml", redSmokes.getDataFolder());
        final BufferedWriter bWriter = new BufferedWriter(new FileWriter(tempFile));
        do {
            final String line = bReader.readLine();
            if (line == null) {
                break;
            }
            if (line.matches(regex)) {
                if (!needUpdate && info != null) {
                    bWriter.write(info, 0, info.length());
                    bWriter.newLine();
                }
                needUpdate = true;
            } else {
                if (line.endsWith("\r\n")) {
                    bWriter.write(line, 0, line.length() - 2);
                } else if (line.endsWith("\r") || line.endsWith("\n")) {
                    bWriter.write(line, 0, line.length() - 1);
                } else {
                    bWriter.write(line, 0, line.length());
                }
                bWriter.newLine();
            }
        } while (true);
        bReader.close();
        bWriter.close();
        if (needUpdate) {
            if (!file.renameTo(new File(file.getParentFile(), file.getName().concat("." + System.currentTimeMillis() + ".upgradebackup")))) {
                throw new Exception(tl("configFileMoveError"));
            }
            if (!tempFile.renameTo(file)) {
                throw new Exception(tl("configFileRenameError"));
            }
        } else {
            tempFile.delete();
        }
    }

    private void updateUsersHomesFormat() {
        if (doneFile.getBoolean("updateUsersHomesFormat", false)) {
            return;
        }
        final File userdataFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            return;
        }
        final File[] userFiles = userdataFolder.listFiles();

        for (final File file : userFiles) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) {
                continue;
            }
            final RedSmokesConfiguration config = new RedSmokesConfiguration(file);
            try {

                config.load();
                if (config.hasProperty("home") && config.hasProperty("home.default")) {
                    final String defworld = config.getString("home.default", null);
                    final Location defloc = getFakeLocation(config.getRootNode(), "home.worlds." + defworld);
                    if (defloc != null) {
                        config.setProperty("homes.home", defloc);
                    }

                    final Set<String> worlds = ConfigurateUtil.getKeys(config.getSection("home.worlds"));
                    Location loc;
                    String worldName;

                    if (worlds.isEmpty()) {
                        continue;
                    }
                    for (final String world : worlds) {
                        if (defworld.equalsIgnoreCase(world)) {
                            continue;
                        }
                        loc = getFakeLocation(config.getRootNode(), "home.worlds." + world);
                        if (loc == null) {
                            continue;
                        }
                        worldName = loc.getWorld().getName().toLowerCase(Locale.ENGLISH);
                        config.setProperty("homes." + worldName, loc);
                    }
                    config.removeProperty("home");
                    config.blockingSave();
                }

            } catch (final RuntimeException ex) {
                redSmokes.getLogger().log(Level.INFO, "File: " + file);
                throw ex;
            }
        }
        doneFile.setProperty("updateUsersHomesFormat", true);
        doneFile.save();
    }

    private void sanitizeAllUserFilenames() {
        if (doneFile.getBoolean("sanitizeAllUserFilenames", false)) {
            return;
        }
        final File usersFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!usersFolder.exists()) {
            return;
        }
        final File[] listOfFiles = usersFolder.listFiles();
        for (final File listOfFile : listOfFiles) {
            final String filename = listOfFile.getName();
            if (!listOfFile.isFile() || !filename.endsWith(".yml")) {
                continue;
            }
            final String sanitizedFilename = StringUtil.sanitizeFileName(filename.substring(0, filename.length() - 4)) + ".yml";
            if (sanitizedFilename.equals(filename)) {
                continue;
            }
            final File tmpFile = new File(listOfFile.getParentFile(), sanitizedFilename + ".tmp");
            final File newFile = new File(listOfFile.getParentFile(), sanitizedFilename);
            if (!listOfFile.renameTo(tmpFile)) {
                redSmokes.getLogger().log(Level.WARNING, tl("userdataMoveError", filename, sanitizedFilename));
                continue;
            }
            if (newFile.exists()) {
                redSmokes.getLogger().log(Level.WARNING, tl("duplicatedUserdata", filename, sanitizedFilename));
                continue;
            }
            if (!tmpFile.renameTo(newFile)) {
                redSmokes.getLogger().log(Level.WARNING, tl("userdataMoveBackError", sanitizedFilename, sanitizedFilename));
            }
        }
        doneFile.setProperty("sanitizeAllUserFilenames", true);
        doneFile.save();
    }

    private World getFakeWorld(final String name) {
        final File bukkitDirectory = redSmokes.getDataFolder().getParentFile().getParentFile();
        final File worldDirectory = new File(bukkitDirectory, name);
        if (worldDirectory.exists() && worldDirectory.isDirectory()) {
            return new FakeWorld(worldDirectory.getName(), World.Environment.NORMAL);
        }
        return null;
    }

    public Location getFakeLocation(final CommentedConfigurationNode config, final String path) {
        final String worldName = config.getString((path != null ? path + "." : "") + "world");
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        final World world = getFakeWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, config.node("x").getDouble(0), config.node("y").getDouble(0),
                config.node("z").getDouble(0), config.node("yaw").getFloat(0), config.node("pitch").getFloat(0));
    }

    private void deleteOldItemsCsv() {
        if (doneFile.getBoolean("deleteOldItemsCsv", false)) {
            return;
        }
        final File file = new File(redSmokes.getDataFolder(), "items.csv");
        if (file.exists()) {
            try {
                final Set<BigInteger> oldconfigs = new HashSet<>();
                oldconfigs.add(new BigInteger("66ec40b09ac167079f558d1099e39f10", 16)); // sep 1
                oldconfigs.add(new BigInteger("34284de1ead43b0bee2aae85e75c041d", 16)); // crlf
                oldconfigs.add(new BigInteger("c33bc9b8ee003861611bbc2f48eb6f4f", 16)); // jul 24
                oldconfigs.add(new BigInteger("6ff17925430735129fc2a02f830c1daa", 16)); // crlf

                final MessageDigest digest = ManagedFile.getDigest();
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                final byte[] buffer = new byte[1024];
                try (final DigestInputStream dis = new DigestInputStream(bis, digest)) {
                    while (dis.read(buffer) != -1) {
                    }
                }

                final BigInteger hash = new BigInteger(1, digest.digest());
                if (oldconfigs.contains(hash) && !file.delete()) {
                    throw new IOException("Could not delete file " + file);
                }
                doneFile.setProperty("deleteOldItemsCsv", true);
                doneFile.save();
            } catch (final IOException ex) {
                redSmokes.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private void warnMetrics() {
        if (doneFile.getBoolean("warnMetrics", false)) {
            return;
        }
        doneFile.setProperty("warnMetrics", true);
        doneFile.save();
    }

    private void uuidFileChange() {
        if (doneFile.getBoolean("uuidFileChange", false)) {
            return;
        }

        final Boolean ignoreUFCache = doneFile.getBoolean("ignore-userfiles-cache", false);

        final File userdir = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdir.exists()) {
            return;
        }

        int countFiles = 0;
        int countReqFiles = 0;
        for (final String string : userdir.list()) {
            if (!string.endsWith(".yml") || string.length() < 5) {
                continue;
            }

            countFiles++;

            final String name = string.substring(0, string.length() - 4);
            UUID uuid = null;

            try {
                uuid = UUID.fromString(name);
            } catch (final IllegalArgumentException ex) {
                countReqFiles++;
            }

            if (countFiles > 100) {
                break;
            }
        }

        if (countReqFiles < 1) {
            return;
        }

        redSmokes.getLogger().info("#### Starting RedSmokes UUID userdata conversion in a few seconds. ####");
        redSmokes.getLogger().info("We recommend you take a backup of your server before upgrading from the old username system.");

        try {
            Thread.sleep(15000);
        } catch (final InterruptedException ex) {
            // NOOP
        }

        uuidFileConvert(redSmokes, ignoreUFCache);

        doneFile.setProperty("uuidFileChange", true);
        doneFile.save();
    }

    private void repairUserMap() {
        if (doneFile.getBoolean("userMapRepaired", false)) {
            return;
        }
        redSmokes.getLogger().info("Starting usermap repair");

        final File userdataFolder = new File(redSmokes.getDataFolder(), "userdata");
        if (!userdataFolder.isDirectory()) {
            redSmokes.getLogger().warning("Missing userdata folder, aborting");
            return;
        }
        final File[] files = userdataFolder.listFiles(YML_FILTER);

        final DecimalFormat format = new DecimalFormat("#0.00");
        final Map<String, UUID> names = Maps.newHashMap();

        for (int index = 0; index < files.length; index++) {
            final File file = files[index];
            try {
                UUID uuid = null;
                final String filename = file.getName();
                final String configData = new String(java.nio.file.Files.readAllBytes(file.toPath()), Charsets.UTF_8);

                if (filename.length() > 36) {
                    try {
                        // ".yml" ending has 4 chars...
                        uuid = UUID.fromString(filename.substring(0, filename.length() - 4));
                    } catch (final IllegalArgumentException ignored) {
                    }
                }

                final Matcher uuidMatcher = PATTERN_CONFIG_UUID.matcher(configData);
                if (uuidMatcher.find()) {
                    try {
                        uuid = UUID.fromString(uuidMatcher.group(1));
                    } catch (final IllegalArgumentException ignored) {
                    }
                }

                if (uuid == null) {
                    // Don't import
                    continue;
                }

                final Matcher nameMatcher = PATTERN_CONFIG_NAME.matcher(configData);
                if (nameMatcher.find()) {
                    final String username = nameMatcher.group(1);
                    if (username != null && username.length() > 0) {
                        names.put(StringUtil.safeString(username), uuid);
                    }
                }

                if (index % 1000 == 0) {
                    redSmokes.getLogger().info("Reading: " + format.format((100d * (double) index) / files.length)
                            + "%");
                }
            } catch (final IOException e) {
                redSmokes.getLogger().log(Level.SEVERE, "Error while reading file: ", e);
                return;
            }
        }

        redSmokes.getUserMap().getNames().putAll(names);
        redSmokes.getUserMap().reloadConfig();

        doneFile.setProperty("userMapRepaired", true);
        doneFile.save();
        redSmokes.getLogger().info("Completed usermap repair.");
    }

    public void beforeSettings() {
        if (!redSmokes.getDataFolder().exists()) {
            redSmokes.getDataFolder().mkdirs();
        }
    }

    public void afterSettings() {
        sanitizeAllUserFilenames();
        uuidFileChange();
        warnMetrics();
        repairUserMap();
        purgeBrokenNpcAccounts();
    }
}
