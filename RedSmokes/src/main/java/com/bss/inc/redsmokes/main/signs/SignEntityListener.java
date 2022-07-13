package com.bss.inc.redsmokes.main.signs;

import com.bss.inc.redsmokes.main.utils.MaterialUtil;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SignEntityListener implements Listener {

    private final transient IRedSmokes redSmokes;

    public SignEntityListener(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignEntityExplode(final EntityExplodeEvent event) {
        if (redSmokes.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        for (final Block block : event.blockList()) {
            if ((MaterialUtil.isSign(block.getType()) && RedSmokesSign.isValidSign(redSmokes, new RedSmokesSign.BlockSign(block))) || RedSmokesSign.checkIfBlockBreaksSigns(block)) {
                event.setCancelled(true);
                return;
            }
            for (final RedSmokesSign sign : redSmokes.getSettings().enabledSigns()) {
                if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType())) {
                    event.setCancelled(!sign.onBlockExplode(block, redSmokes));
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignEntityChangeBlock(final EntityChangeBlockEvent event) {
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
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, redSmokes)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
