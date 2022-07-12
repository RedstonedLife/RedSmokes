package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.OfflinePlayer;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.services.BalanceTop;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BalanceTopImpl implements BalanceTop {
    private final IRedSmokes redSmokes;
    private LinkedHashMap<UUID, BalanceTop.Entry> topCache = new LinkedHashMap<>();
    private BigDecimal balanceTopTotal = BigDecimal.ZERO;
    private long cacheAge = 0;
    private CompletableFuture<Void> cacheLock;

    public BalanceTopImpl(IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        redSmokes.getServer().getServicesManager().register(BalanceTop.class, this, redSmokes, ServicePriority.Normal);
    }

    private void calculateBalanceTopMap() {
        final List<Entry> entries = new LinkedList<>();
        BigDecimal newTotal = BigDecimal.ZERO;
        for(UUID u : redSmokes.getUserMap().getAllUniqueUsers()) {
            final User user = redSmokes.getUserMap().getUser(u);
            if(user != null) {
                if(!redSmokes.getSettings().isNpcsInBalanceRanking() && user.isNPC()) {
                    // Don't list NPCs in output
                    continue;
                }
                if(!user.isBaltopExempt()) {
                    final BigDecimal userMoney = user.getMoney();
                    user.updateMoneyCache(userMoney);
                    newTotal = newTotal.add(userMoney);
                    final String name;
                    if(user.getBase() instanceof OfflinePlayer){name = user.getLastAccountName();}
                    else if(user.isHidden()){name=user.getName();}
                    else{name=user.getDisplayName();}
                    entries.add(new BalanceTop.Entry(user.getUUID(), name, userMoney));
                }
            }
        }
        final LinkedHashMap<UUID, Entry> sortedMap = new LinkedHashMap<>();
        entries.sort((entry1, entry2) -> entry2.getBalance().compareTo(entry1.getBalance()));
        for (Entry entry : entries) {
            sortedMap.put(entry.getUuid(), entry);
        }
        topCache = sortedMap;
        balanceTopTotal = newTotal;
        cacheAge = System.currentTimeMillis();
        cacheLock.complete(null);
        cacheLock = null;
    }

    @Override
    public CompletableFuture<Void> calculateBalanceTopMapAsync() {
        if (cacheLock != null) {
            return cacheLock;
        }
        cacheLock = new CompletableFuture<>();
        redSmokes.runTaskAsynchronously(this::calculateBalanceTopMap);
        return cacheLock;
    }

    @Override
    public Map<UUID, Entry> getBalanceTopCache() {
        return Collections.unmodifiableMap(topCache);
    }

    @Override
    public long getCacheAge() {
        return cacheAge;
    }

    @Override
    public BigDecimal getBalanceTopTotal() {
        return balanceTopTotal;
    }

    public boolean isCacheLocked() {
        return cacheLock != null;
    }
}
