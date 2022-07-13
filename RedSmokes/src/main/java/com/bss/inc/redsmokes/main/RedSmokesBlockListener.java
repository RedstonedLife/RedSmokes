package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.MaterialUtil;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class RedSmokesBlockListener implements Listener {
    private final transient IRedSmokes redSmokes;

    public RedSmokesBlockListener(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final ItemStack is = event.getItemInHand();

        if (is.getType() == MaterialUtil.SPAWNER && redSmokes.getPersistentDataProvider().getString(is, "convert") != null) {
            final BlockState blockState = event.getBlockPlaced().getState();
            if (blockState instanceof CreatureSpawner) {
                final CreatureSpawner spawner = (CreatureSpawner) blockState;
                final EntityType type = redSmokes.getSpawnerItemProvider().getEntityType(event.getItemInHand());
                if (type != null && Mob.fromBukkitType(type) != null) {
                    if (redSmokes.getUser(event.getPlayer()).isAuthorized("redsmokes.spawnerconvert." + Mob.fromBukkitType(type).name().toLowerCase(Locale.ENGLISH))) {
                        spawner.setSpawnedType(type);
                        spawner.update();
                    }
                }
            }
        }

        final User user = redSmokes.getUser(event.getPlayer());
        if (user.hasUnlimited(is) && user.getBase().getGameMode() == GameMode.SURVIVAL) {
            redSmokes.scheduleSyncDelayedTask(() -> {
                if (is != null && is.getType() != null && !MaterialUtil.isAir(is.getType())) {
                    final ItemStack cloneIs = is.clone();
                    cloneIs.setAmount(1);
                    user.getBase().getInventory().addItem(cloneIs);
                    user.getBase().updateInventory();
                }
            });
        }
    }
}
