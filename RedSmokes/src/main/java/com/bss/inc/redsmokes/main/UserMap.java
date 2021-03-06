package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.OfflinePlayer;
import com.google.common.cache.*;
import net.redsmokes.api.IConf;
import net.redsmokes.api.MaxMoneyException;
import com.bss.inc.redsmokes.main.api.UserDoesNotExistException;
import com.bss.inc.redsmokes.main.utils.StringUtil;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class UserMap extends CacheLoader<String, User> implements IConf {
    private static boolean legacy = false;
    private static Method getLegacy;
    private final transient net.redsmokes.api.IRedSmokes redSmokes;
    private final transient ConcurrentSkipListSet<UUID> keys = new ConcurrentSkipListSet<>();
    private final transient ConcurrentSkipListMap<String, UUID> names = new ConcurrentSkipListMap<>();
    private final transient ConcurrentSkipListMap<UUID, ArrayList<String>> history = new ConcurrentSkipListMap<>();
    private final UUIDMap uuidMap;
    private final transient Cache<String, User> users;
    private final Pattern validUserPattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    private static final String WARN_UUID_NOT_REPLACE = "Found UUID {0} for player {1}, but player already has a UUID ({2}). Not replacing UUID in usermap.";

    public UserMap(final net.redsmokes.api.IRedSmokes redSmokes) {
        super();
        this.redSmokes=redSmokes;
        uuidMap = new UUIDMap(redSmokes);
        //RemovalListener<UUID, User> remListener = new UserMapRemovalListener();
        //users = CacheBuilder.newBuilder().maximumSize(redSmokes.getSettings().getMaxUserCacheCount()).softValues().removalListener(remListener).build(this);
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        final int maxCount =redSmokes.getSettings().getMaxUserCacheCount();
        try {
            cacheBuilder.maximumSize(maxCount);
        } catch (final NoSuchMethodError nsme) {
            legacy = true;
            legacyMaximumSize(cacheBuilder, maxCount);
        }
        cacheBuilder.softValues();
        if (!legacy) {
            users = cacheBuilder.build(this);
        } else {
            users = legacyBuild(cacheBuilder);
        }
    }

    private void loadAllUsersAsync(final IRedSmokes ess) {
        ess.runTaskAsynchronously(() -> {
            synchronized (users) {
                final File userdir = new File(ess.getDataFolder(), "userdata");
                if (!userdir.exists()) {
                    return;
                }
                keys.clear();
                users.invalidateAll();
                for (final String string : userdir.list()) {
                    if (!string.endsWith(".yml")) {
                        continue;
                    }
                    final String name = string.substring(0, string.length() - 4);
                    try {
                        keys.add(UUID.fromString(name));
                    } catch (final IllegalArgumentException ex) {
                        //Ignore these users till they rejoin.
                    }
                }
                uuidMap.loadAllUsers(names, history);
            }
        });
    }

    public boolean userExists(final UUID uuid) {
        return keys.contains(uuid);
    }

    public User getUser(final String name) {
        final String sanitizedName = StringUtil.safeString(name);
        try {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().warning("Looking up username " + name + " (" + sanitizedName + ") ...");
            }

            if (names.containsKey(sanitizedName)) {
                final UUID uuid = names.get(sanitizedName);
                return getUser(uuid);
            }

            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().warning(name + "(" + sanitizedName + ") has no known usermap entry");
            }

            final File userFile = getUserFileFromString(sanitizedName);
            if (userFile.exists()) {
                redSmokes.getLogger().info("Importing user " + name + " to usermap.");
                final User user = new User(new OfflinePlayer(sanitizedName, redSmokes.getServer()), redSmokes);
                trackUUID(user.getBase().getUniqueId(), user.getName(), true);
                return user;
            }
            return null;
        } catch (final UncheckedExecutionException ex) {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().log(Level.WARNING, ex, () -> String.format("Exception while getting user for %s (%s)", name, sanitizedName));
            }
            return null;
        }
    }

    public User getUser(final UUID uuid) {
        try {
            if (!legacy) {
                return ((LoadingCache<String, User>) users).get(uuid.toString());
            } else {
                return legacyCacheGet(uuid);
            }
        } catch (final ExecutionException | UncheckedExecutionException ex) {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().log(Level.WARNING, ex, () -> "Exception while getting user for " + uuid);
            }
            return null;
        }
    }

    public void trackUUID(final UUID uuid, final String name, final boolean replace) {
        if (uuid != null) {
            keys.add(uuid);
            if (name != null && name.length() > 0) {
                final String keyName = redSmokes.getSettings().isSafeUsermap() ? StringUtil.safeString(name) : name;
                if (!names.containsKey(keyName)) {
                    names.put(keyName, uuid);
                    uuidMap.writeUUIDMap();
                } else if (!isUUIDMatch(uuid, keyName)) {
                    if (replace) {
                        redSmokes.getLogger().info("Found new UUID for " + name + ". Replacing " + names.get(keyName).toString() + " with " + uuid);
                        names.put(keyName, uuid);
                        uuidMap.writeUUIDMap();
                    } else {
                        redSmokes.getLogger().log(Level.INFO, MessageFormat.format(WARN_UUID_NOT_REPLACE, uuid.toString(), name, names.get(keyName).toString()), new RuntimeException());
                    }
                }
            }
        }
    }

    public boolean isUUIDMatch(final UUID uuid, final String name) {
        return names.containsKey(name) && names.get(name).equals(uuid);
    }

    @Override
    public User load(final String stringUUID) throws Exception {
        final UUID uuid = UUID.fromString(stringUUID);
        Player player = redSmokes.getServer().getPlayer(uuid);
        if (player != null) {
            final User user = new User(player, redSmokes);
            trackUUID(uuid, user.getName(), true);
            return user;
        }

        final File userFile = getUserFileFromID(uuid);

        if (userFile.exists()) {
            player = new OfflinePlayer(uuid, redSmokes.getServer());
            final User user = new User(player, redSmokes);
            ((OfflinePlayer) player).setName(user.getLastAccountName());
            trackUUID(uuid, user.getName(), false);
            return user;
        }

        throw new Exception("User not found!");
    }

    public User load(final org.bukkit.OfflinePlayer player) throws UserDoesNotExistException {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null!");
        }

        if (player instanceof Player) {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().info("Loading online OfflinePlayer into user map...");
            }
            final User user = new User((Player) player, redSmokes);
            trackUUID(player.getUniqueId(), player.getName(), true);
            return user;
        }

        final File userFile = getUserFileFromID(player.getUniqueId());
        if (redSmokes.getSettings().isDebug()) {
            redSmokes.getLogger().info("Loading OfflinePlayer into user map. Has data: " + userFile.exists() + " for " + player);
        }

        final OfflinePlayer redPlayer = new OfflinePlayer(player.getUniqueId(), redSmokes.getServer());
        final User user = new User(redPlayer, redSmokes);
        if (userFile.exists()) {
            redPlayer.setName(user.getLastAccountName());
        } else {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().info("OfflinePlayer usermap load saving user data for " + player);
            }

            // this code makes me sad
            user.startTransaction();
            try {
                user.setMoney(redSmokes.getSettings().getStartingBalance());
            } catch (MaxMoneyException e) {
                // Shouldn't happen as it would be an illegal configuration state
                throw new RuntimeException(e);
            }
            user.setLastAccountName(user.getName());
            user.stopTransaction();
        }

        trackUUID(player.getUniqueId(), user.getName(), false);
        return user;
    }

    @Override
    public void reloadConfig() {
        getUUIDMap().forceWriteUUIDMap();
        loadAllUsersAsync(redSmokes);
    }

    public void invalidateAll() {
        users.invalidateAll();
    }

    public void removeUser(final String name) {
        if (names == null) {
            redSmokes.getLogger().warning("Name collection is null, cannot remove user.");
            return;
        }
        final UUID uuid = names.get(name);
        if (uuid != null) {
            keys.remove(uuid);
            users.invalidate(uuid.toString());
        }
        names.remove(name);
        names.remove(StringUtil.safeString(name));
    }

    public void removeUserUUID(final String uuid) {
        users.invalidate(uuid);
    }

    public Set<UUID> getAllUniqueUsers() {
        return Collections.unmodifiableSet(keys);
    }

    public int getUniqueUsers() {
        return keys.size();
    }

    protected ConcurrentSkipListMap<String, UUID> getNames() {
        return names;
    }

    protected ConcurrentSkipListMap<UUID, ArrayList<String>> getHistory() {
        return history;
    }

    public List<String> getUserHistory(final UUID uuid) {
        return history.get(uuid);
    }

    public UUIDMap getUUIDMap() {
        return uuidMap;
    }
    //  class UserMapRemovalListener implements RemovalListener
    //  {
    //      @Override
    //      public void onRemoval(final RemovalNotification notification)
    //      {
    //          Object value = notification.getValue();
    //          if (value != null)
    //          {
    //              ((User)value).cleanup();
    //          }
    //      }
    //  }

    private File getUserFileFromID(final UUID uuid) {
        final File userFolder = new File(redSmokes.getDataFolder(), "userdata");
        return new File(userFolder, uuid.toString() + ".yml");
    }

    public File getUserFileFromString(final String name) {
        final File userFolder = new File(redSmokes.getDataFolder(), "userdata");
        return new File(userFolder, StringUtil.sanitizeFileName(name) + ".yml");
    }

    @SuppressWarnings("deprecation")
    public User getUserFromBukkit(String name) {
        name = StringUtil.safeString(name);
        if (redSmokes.getSettings().isDebug()) {
            redSmokes.getLogger().warning("Using potentially blocking Bukkit UUID lookup for: " + name);
        }
        // Don't attempt to look up entirely invalid usernames
        if (name == null || !validUserPattern.matcher(name).matches()) {
            return null;
        }
        final org.bukkit.OfflinePlayer offlinePlayer = redSmokes.getServer().getOfflinePlayer(name);
        if (offlinePlayer == null) {
            return null;
        }
        final UUID uuid;
        try {
            uuid = offlinePlayer.getUniqueId();
        } catch (final UnsupportedOperationException | NullPointerException e) {
            return null;
        }
        // This is how Bukkit generates fake UUIDs
        if (UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)).equals(uuid)) {
            return null;
        } else {
            names.put(name, uuid);
            return getUser(uuid);
        }
    }

    private User legacyCacheGet(final UUID uuid) {
        if (getLegacy == null) {
            final Class<?> usersClass = users.getClass();
            for (final Method m : usersClass.getDeclaredMethods()) {
                if (m.getName().equals("get")) {
                    getLegacy = m;
                    getLegacy.setAccessible(true);
                    break;
                }
            }
        }
        try {
            return (User) getLegacy.invoke(users, uuid.toString());
        } catch (final IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private void legacyMaximumSize(final CacheBuilder builder, final int maxCount) {
        try {
            final Method maxSizeLegacy = builder.getClass().getDeclaredMethod("maximumSize", Integer.TYPE);
            maxSizeLegacy.setAccessible(true);
            maxSizeLegacy.invoke(builder, maxCount);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Cache<String, User> legacyBuild(final CacheBuilder builder) {
        Method build = null;
        for (final Method method : builder.getClass().getDeclaredMethods()) {
            if (method.getName().equals("build")) {
                build = method;
                break;
            }
        }
        Cache<String, User> legacyUsers;
        try {
            assert build != null;
            build.setAccessible(true);
            legacyUsers = (Cache<String, User>) build.invoke(builder, this);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            legacyUsers = null;
        }
        return legacyUsers;
    }
}
