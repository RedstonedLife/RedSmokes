package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import org.bukkit.inventory.ItemStack;

import java.io.FileWriter;
import java.math.BigDecimal;

public class Trade {
    private static FileWriter fw = null;
    private final transient String command;
    private final transient Trade fallbackTrade;
    private final transient BigDecimal money;
    private final transient ItemStack itemStack;
    private final transient Integer exp;
    private final transient IRedSmokes redSmokes;

    public Trade(final String command, final IRedSmokes redSmokes) {
        this(command, null, null, null, null, redSmokes);
    }

    public Trade(final String command, final Trade fallback, final IRedSmokes redSmokes) {
        this(command, fallback, null, null, null, redSmokes);
    }

    public Trade(final BigDecimal money, final IRedSmokes redSmokes) {
        this(null, null, money, null, null, redSmokes);
    }

    public Trade(final ItemStack items, final IRedSmokes redSmokes) {
        this(null, null, null, items, null, redSmokes);
    }

    public Trade(final int exp, final IRedSmokes redSmokes) {
        this(null, null, null, null, exp, redSmokes);
    }

    public Trade(final String command, final Trade fallback, final BigDecimal money, final ItemStack item, final Integer exp, final IRedSmokes redSmokes) {
        this.command = command;
        this.fallbackTrade = fallback;
        this.money = money;
        this.itemStack = item;
        this.exp = exp;
        this.redSmokes = redSmokes;
    }

    
}
