package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.MaterialUtil;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.earth2me.essentials.I18n.tl;

public class EssentialsEntityListener implements Listener {
    private static final transient Pattern powertoolPlayer = Pattern.compile("\\{player\\}");
    private final IEssentials ess;

    public EssentialsEntityListener(final IEssentials ess) {
        this.ess = ess;
    }

    // This method does something undocumented reguarding certain bucket types #EasterEgg
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        final Entity eAttack = event.getDamager();
        final Entity eDefend = event.getEntity();
        if (eAttack instanceof Player) {
            final User attacker = ess.getUser((Player) eAttack);
            if (eDefend instanceof Player) {
                onPlayerVsPlayerDamage(event, (Player) eDefend, attacker);
            } else if (eDefend instanceof Ageable) {
                final ItemStack hand = attacker.getBase().getItemInHand();
                if (ess.getSettings().isMilkBucketEasterEggEnabled()
                        && hand != null && hand.getType() == Material.MILK_BUCKET) {
                    ((Ageable) eDefend).setBaby();
                    hand.setType(Material.BUCKET);
                    attacker.getBase().setItemInHand(hand);
                    attacker.getBase().updateInventory();
                    event.setCancelled(true);
                }
            }
            attacker.updateActivityOnInteract(true);
        } else if (eAttack instanceof Projectile && eDefend instanceof Player) {
            final Projectile projectile = (Projectile) event.getDamager();
            //This should return a ProjectileSource on 1.7.3 beta +
            final Object shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                final User attacker = ess.getUser((Player) shooter);
                onPlayerVsPlayerDamage(event, (Player) eDefend, attacker);
                attacker.updateActivityOnInteract(true);
            }
        }
    }

    private void onPlayerVsPlayerDamage(final EntityDamageByEntityEvent event, final Player defender, final User attacker) {
        if (ess.getSettings().getLoginAttackDelay() > 0 && (System.currentTimeMillis() < (attacker.getLastLogin() + ess.getSettings().getLoginAttackDelay())) && !attacker.isAuthorized("essentials.pvpdelay.exempt")) {
            event.setCancelled(true);
        }

        if (!defender.equals(attacker.getBase()) && (attacker.hasInvulnerabilityAfterTeleport() || ess.getUser(defender).hasInvulnerabilityAfterTeleport())) {
            event.setCancelled(true);
        }

        if (attacker.isGodModeEnabled() && !attacker.isAuthorized("essentials.god.pvp")) {
            event.setCancelled(true);
        }

        if (attacker.isHidden() && !attacker.isAuthorized("essentials.vanish.pvp")) {
            event.setCancelled(true);
        }

        if (attacker.arePowerToolsEnabled()) {
            onPlayerVsPlayerPowertool(event, defender, attacker);
        }
    }

    private void onPlayerVsPlayerPowertool(final EntityDamageByEntityEvent event, final Player defender, final User attacker) {
        final List<String> commandList = attacker.getPowertool(attacker.getBase().getItemInHand());
        if (commandList != null && !commandList.isEmpty()) {
            for (final String tempCommand : commandList) {
                final String command = powertoolPlayer.matcher(tempCommand).replaceAll(defender.getName());
                if (command != null && !command.isEmpty() && !command.equals(tempCommand)) {

                    class PowerToolInteractTask implements Runnable {
                        @Override
                        public void run() {
                            attacker.getBase().chat("/" + command);
                            ess.getLogger().log(Level.INFO, String.format("[PT] %s issued server command: /%s", attacker.getName(), command));
                        }
                    }

                    ess.scheduleSyncDelayedTask(new PowerToolInteractTask());

                    event.setCancelled(true);
                    return;
                }
            }
        }
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
