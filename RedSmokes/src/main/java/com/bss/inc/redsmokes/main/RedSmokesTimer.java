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

}
