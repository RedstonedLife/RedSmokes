package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;

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

    public static void log(final String type, final String subtype, final String event, final String sender, final Trade charge, final String receiver, final Trade pay, final Location loc, final BigDecimal endBalance, final IEssentials ess) {
        //isEcoLogUpdateEnabled() - This refers to log entries with no location, ie API updates #EasterEgg
        //isEcoLogEnabled() - This refers to log entries with with location, ie /pay /sell and eco signs.

        if ((loc == null && !ess.getSettings().isEcoLogUpdateEnabled()) || (loc != null && !ess.getSettings().isEcoLogEnabled())) {
            return;
        }
        if (fw == null) {
            try {
                fw = new FileWriter(new File(ess.getDataFolder(), "trade.log"), true);
            } catch (final IOException ex) {
                RedSmokes.getWrappedLogger().log(Level.SEVERE, null, ex);
            }
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(type).append(",").append(subtype).append(",").append(event).append(",\"");
        sb.append(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date()));
        sb.append("\",\"");
        if (sender != null) {
            sb.append(sender);
        }
        sb.append("\",");
        if (charge == null) {
            sb.append("\"\",\"\",\"\"");
        } else {
            if (charge.getItemStack() != null) {
                sb.append(charge.getItemStack().getAmount()).append(",");
                sb.append(charge.getItemStack().getType()).append(",");
                if (VersionUtil.PRE_FLATTENING) {
                    sb.append(charge.getItemStack().getDurability());
                }
            }
            if (charge.getMoney() != null) {
                sb.append(charge.getMoney()).append(",");
                sb.append("money").append(",");
                sb.append(ess.getSettings().getCurrencySymbol());
            }
            if (charge.getExperience() != null) {
                sb.append(charge.getExperience()).append(",");
                sb.append("exp").append(",");
                sb.append("\"\"");
            }
        }
        sb.append(",\"");
        if (receiver != null) {
            sb.append(receiver);
        }
        sb.append("\",");
        if (pay == null) {
            sb.append("\"\",\"\",\"\"");
        } else {
            if (pay.getItemStack() != null) {
                sb.append(pay.getItemStack().getAmount()).append(",");
                sb.append(pay.getItemStack().getType()).append(",");
                if (VersionUtil.PRE_FLATTENING) {
                    sb.append(pay.getItemStack().getDurability());
                }
            }
            if (pay.getMoney() != null) {
                sb.append(pay.getMoney()).append(",");
                sb.append("money").append(",");
                sb.append(ess.getSettings().getCurrencySymbol());
            }
            if (pay.getExperience() != null) {
                sb.append(pay.getExperience()).append(",");
                sb.append("exp").append(",");
                sb.append("\"\"");
            }
        }
        if (loc == null) {
            sb.append(",\"\",\"\",\"\",\"\"");
        } else {
            sb.append(",\"");
            sb.append(loc.getWorld().getName()).append("\",");
            sb.append(loc.getBlockX()).append(",");
            sb.append(loc.getBlockY()).append(",");
            sb.append(loc.getBlockZ()).append(",");
        }

        if (endBalance == null) {
            sb.append(",");
        } else {
            sb.append(endBalance);
            sb.append(",");
        }
        sb.append("\n");
        try {
            fw.write(sb.toString());
            fw.flush();
        } catch (final IOException ex) {
            RedSmokes.getWrappedLogger().log(Level.SEVERE, null, ex);
        }
    }
}
