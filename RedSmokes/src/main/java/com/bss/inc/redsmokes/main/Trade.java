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
}
