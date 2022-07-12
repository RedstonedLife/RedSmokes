package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.api.services.BalanceTop;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
                    
                }
            }
        }
    }
}
