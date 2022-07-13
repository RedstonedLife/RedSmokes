package com.bss.inc.redsmokes.main.signs;

import com.bss.inc.redsmokes.main.I18n;
import com.bss.inc.redsmokes.main.User;
import com.bss.inc.redsmokes.main.utils.FormatUtil;
import com.bss.inc.redsmokes.main.utils.MaterialUtil;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.MaxMoneyException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.logging.Level;

public class SignBlockListener implements Listener {
    private final transient IRedSmokes redSmokes;

    public SignBlockListener(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBlockBreak(final BlockBreakEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }
        try {
            if (protectSignsAndBlocks(event.getBlock(), event.getPlayer())) {
                event.setCancelled(true);
            }
        } catch (final MaxMoneyException ex) {
            event.setCancelled(true);
        }
    }

    public boolean protectSignsAndBlocks(final Block block, final Player player) throws MaxMoneyException {
        // prevent any signs be broken by destroying the block they are attached to
        if (RedSmokesSign.checkIfBlockBreaksSigns(block)) {
            if (redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().log(Level.INFO, "Prevented that a block was broken next to a sign.");
            }
            return true;
        }

        final Material mat = block.getType();
        if (MaterialUtil.isSign(mat)) {
            final Sign csign = (Sign) block.getState();

            for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
                if (csign.getLine(0).equalsIgnoreCase(sign.getSuccessName(redSmokes)) && !sign.onSignBreak(block, player, redSmokes)) {
                    return true;
                }
            }
        }

        for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, player, redSmokes)) {
                redSmokes.getLogger().log(Level.INFO, "A block was protected by a sign.");
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignSignChange2(final SignChangeEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }
        final User user = redSmokes.getUser(event.getPlayer());

        for (int i = 0; i < 4; i++) {
            event.setLine(i, FormatUtil.formatString(user, "redsmokes.signs", event.getLine(i)));
        }

        final String lColorlessTopLine = ChatColor.stripColor(event.getLine(0)).toLowerCase().trim();
        if (lColorlessTopLine.isEmpty()) {
            return;
        }
        //We loop through all sign types here to prevent clashes with preexisting signs later
        for (final Signs signs : Signs.values()) {
            final RedSmokesSign sign = signs.getSign();
            // If the top sign line contains any of the success name (excluding colors), just remove all colours from the first line.
            // This is to ensure we are only modifying possible Essentials Sign and not just removing colors from the first line of all signs.
            // Top line and sign#getSuccessName() are both lowercased since contains is case-sensitive.
            final String successName = sign.getSuccessName(redSmokes);
            if (successName == null) {
                event.getPlayer().sendMessage(I18n.tl("errorWithMessage",
                        "Please report this error to a staff member."));
                return;
            }
            final String lSuccessName = ChatColor.stripColor(successName.toLowerCase());
            if (lColorlessTopLine.contains(lSuccessName)) {

                // If this sign is not enabled and it has been requested to not protect it's name (when disabled), then do not protect the name.
                // By lower-casing it and stripping colours.
                if (!redSmokes.getSettings().enabledSigns().contains(sign)
                        && redSmokes.getSettings().getUnprotectedSignNames().contains(sign)) {
                    continue;
                }
                event.setLine(0, lColorlessTopLine);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignSignChange(final SignChangeEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
            if (event.getLine(0).equalsIgnoreCase(sign.getSuccessName(redSmokes))) {
                event.setCancelled(true);
                return;
            }
            if (event.getLine(0).equalsIgnoreCase(sign.getTemplateName()) && !sign.onSignCreate(event, redSmokes)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignBlockPlace(final BlockPlaceEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        final Block against = event.getBlockAgainst();
        if (MaterialUtil.isSign(against.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(against))) {
            event.setCancelled(true);
            return;
        }
        final Block block = event.getBlock();
        if (MaterialUtil.isSign(block.getType())) {
            return;
        }
        for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPlace(block, event.getPlayer(), ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignBlockBurn(final BlockBurnEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        final Block block = event.getBlock();
        if ((MaterialUtil.isSign(block.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(block))) || RedSmokesSign.checkIfBlockBreaksSigns(block)) {
            event.setCancelled(true);
            return;
        }
        for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBurn(block, redSmokes)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignBlockIgnite(final BlockIgniteEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        final Block block = event.getBlock();
        if ((MaterialUtil.isSign(block.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(block))) || RedSmokesSign.checkIfBlockBreaksSigns(block)) {
            event.setCancelled(true);
            return;
        }
        for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockIgnite(block, redSmokes)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBlockPistonExtend(final BlockPistonExtendEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        for (final Block block : event.getBlocks()) {
            if ((MaterialUtil.isSign(block.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(block))) || RedSmokesSign.checkIfBlockBreaksSigns(block)) {
                event.setCancelled(true);
                return;
            }
            for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
                if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, redSmokes)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBlockPistonRetract(final BlockPistonRetractEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        if (event.isSticky()) {
            final Block pistonBaseBlock = event.getBlock();
            final Block[] affectedBlocks = new Block[] {pistonBaseBlock, pistonBaseBlock.getRelative(event.getDirection()), event.getRetractLocation().getBlock()};

            for (final Block block : affectedBlocks) {
                if ((MaterialUtil.isSign(block.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(block))) || RedSmokesSign.checkIfBlockBreaksSigns(block)) {
                    event.setCancelled(true);
                    return;
                }
                for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
                    if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, redSmokes)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
