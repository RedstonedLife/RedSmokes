package com.bss.inc.redsmokes.main.commands;

import com.bss.inc.redsmokes.main.CommandSource;
import com.bss.inc.redsmokes.main.textreader.SimpleTextInput;
import com.bss.inc.redsmokes.main.textreader.TextPager;
import com.bss.inc.redsmokes.main.utils.NumberUtil;
import com.google.common.collect.Lists;
import net.redsmokes.api.services.BalanceTop;
import org.bukkit.Server;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Commandbalancetop extends RedSmokesCommand {
    public static int MINUSERS = 50;
    private static int CACHETIME = 2 * 60 * 1000;
    private static SimpleTextInput cache = new SimpleTextInput();

    public Commandbalancetop() {
        super("balancetop");
    }

    private void outputCache(final CommandSource sender, final int page) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(redSmokes.getBalanceTop().getCacheAge());
        final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        sender.sendMessage(tl("balanceTop", format.format(cal.getTime())));
        new TextPager(cache).showPage(Integer.toString(page), null, "balancetop", sender);
    }

    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        int page = 0;
        boolean force = false;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ex) {
                if (args[0].equalsIgnoreCase("force") && (!sender.isPlayer() || ess.getUser(sender.getPlayer()).isAuthorized("essentials.balancetop.force"))) {
                    force = true;
                }
            }
        }

        if (!force && redSmokes.getBalanceTop().getCacheAge() > System.currentTimeMillis() - CACHETIME) {
            outputCache(sender, page);
            return;
        }

        // If there are less than 50 users in our usermap, there is no need to display a warning as these calculations should be done quickly
        if (redSmokes.getUserMap().getUniqueUsers() > MINUSERS) {
            sender.sendMessage(tl("orderBalances", redSmokes.getUserMap().getUniqueUsers()));
        }

        redSmokes.runTaskAsynchronously(new Viewer(sender, page, force));
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            final List<String> options = Lists.newArrayList("1");
            if (!sender.isPlayer() || redSmokes.getUser(sender.getPlayer()).isAuthorized("redsmokes.balancetop.force")) {
                options.add("force");
            }
            return options;
        } else {
            return Collections.emptyList();
        }
    }

    private class Viewer implements Runnable {
        private final transient CommandSource sender;
        private final transient int page;
        private final transient boolean force;

        Viewer(final CommandSource sender, final int page, final boolean force) {
            this.sender = sender;
            this.page = page;
            this.force = force;
        }

        @Override
        public void run() {
            if (redSmokes.getSettings().isEcoDisabled()) {
                if (redSmokes.getSettings().isDebug()) {
                    redSmokes.getLogger().info("Internal economy functions disabled, aborting baltop.");
                }
                return;
            }

            final boolean fresh = force || redSmokes.getBalanceTop().isCacheLocked() || redSmokes.getBalanceTop().getCacheAge() <= System.currentTimeMillis() - CACHETIME;
            final CompletableFuture<Void> future = fresh ? redSmokes.getBalanceTop().calculateBalanceTopMapAsync() : CompletableFuture.completedFuture(null);
            future.thenRun(() -> {
                if (fresh) {
                    final SimpleTextInput newCache = new SimpleTextInput();
                    newCache.getLines().add(tl("serverTotal", NumberUtil.displayCurrency(redSmokes.getBalanceTop().getBalanceTopTotal(), redSmokes)));
                    int pos = 1;
                    for (final Map.Entry<UUID, BalanceTop.Entry> entry : redSmokes.getBalanceTop().getBalanceTopCache().entrySet()) {
                        if (redSmokes.getSettings().showZeroBaltop() || entry.getValue().getBalance().compareTo(BigDecimal.ZERO) > 0) {
                            newCache.getLines().add(tl("balanceTopLine", pos, entry.getValue().getDisplayName(), NumberUtil.displayCurrency(entry.getValue().getBalance(), redSmokes)));
                        }
                        pos++;
                    }
                    cache = newCache;
                }

                outputCache(sender, page);
            });
        }
    }
}
