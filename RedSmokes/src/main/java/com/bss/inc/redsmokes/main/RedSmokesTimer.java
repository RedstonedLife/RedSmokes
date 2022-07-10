package com.bss.inc.redsmokes.main;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class RedSmokesTimer implements Runnable {
    private final transient IRedSmokes redSmokes;
    private final transient Set<UUID> onlineUsers = new HashSet<>();
    private final LinkedList<Double> history = new LinkedList<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final long maxTime = 10 * 1000000;
    @SuppressWarnings("FieldCanBeLocal")
    private final long tickInterval = 50;
    private transient long lastPoll = System.nanoTime();
    private int skip1,skip2 = 0;

    RedSmokesTimer(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        history.add(20d);
    }

    @Override
    public void run() {
        final long startTime = System.nanoTime();
        final long currentTime = System.currentTimeMillis();
        long timeSpent = (startTime - lastPoll) / 1000;
        if(timeSpent == 0) {timeSpent = 1;}
        if(history.size() > 10) {history.remove();}
        final double tps = tickInterval * 1000000.0 / timeSpent;
        if(tps <= 21) {history.add(tps);}
        lastPoll = startTime;
        int count = 0;
        onlineUsers.clear();
        for(final Player player : redSmokes.getOnlinePlayers()) {
            count++;
            if(skip1 > 0) {skip1--;continue;}
            if(count % 10 == 0) {
                if(System.nanoTime() - startTime > maxTime / 2) {
                    skip1 = count - 1;
                    break;
                }
            }
            try {
                final User user = redSmokes.getUser(player);
                onlineUsers.add(user.getBase().getUniqueId());
                user.setLastOnlineActivity(currentTime);
                user.checkActivity();
            } catch (final Exception e) {
                redSmokes.getLogger().log(Level.WARNING, "RedSmokesTimer Error: ", e);
            }
        }

        count = 0;
        final Iterator<UUID> iterator = onlineUsers.iterator();
        while (iterator.hasNext()) {
            count++;
            if(skip2 > 0) {skip2--;continue;}
            if(count % 10 == 0) {
        }
    }

}
