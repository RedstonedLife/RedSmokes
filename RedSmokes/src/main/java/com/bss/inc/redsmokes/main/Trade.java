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

    public 
}
