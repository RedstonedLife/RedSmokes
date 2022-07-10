package com.bss.inc.redsmokes.main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

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
        if(timeSpent == 0) {
            timeSpent = 1;
        }
        if(history.size() > 10) {history.remove();}

    }

}
