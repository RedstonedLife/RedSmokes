package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.utils.EnumUtil;
import org.bukkit.Statistic;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

public class User implements com.bss.inc.redsmokes.api.IUser, Comparable<User> {
    private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");

    // User command confirmation strings
    private final Map<User, BigDecimal> confirmingPayments = new WeakHashMap<>();

    // User Properties
    private transient boolean vanished;
}
