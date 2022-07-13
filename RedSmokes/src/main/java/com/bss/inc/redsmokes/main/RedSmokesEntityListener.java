package com.bss.inc.redsmokes.main;

import net.redsmokes.api.IRedSmokes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;


public class RedSmokesEntityListener implements Listener {
    private final IRedSmokes redSmokes;

    public RedSmokesEntityListener(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
    }

    // This method does something undocumented reguarding certain bucket types #EasterEgg
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityCombust(final EntityCombustEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityCombustByEntity(final EntityCombustByEntityEvent event) {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(final PlayerDeathEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeathExpEvent(final PlayerDeathEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeathInvEvent(final PlayerDeathEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplashEvent(final PotionSplashEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityShootBow(final EntityShootBowEvent event) {
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTarget(final EntityTargetEvent event) {
    }
}
