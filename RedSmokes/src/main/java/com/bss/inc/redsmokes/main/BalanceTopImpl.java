package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.api.services.BalanceTop;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BalanceTopImpl implements BalanceTop {
    private final IRedSmokes redSmokes;
    private LinkedHashMap<UUID, BalanceTop.Entry> topCache = new LinkedHashMap<>();
    private BigDecimal balanceTopTotal = BigDecimal.ZERO;
    private long cacheAge = 0;
    private CompletableFuture<Void> cacheLock;

    public 
}
