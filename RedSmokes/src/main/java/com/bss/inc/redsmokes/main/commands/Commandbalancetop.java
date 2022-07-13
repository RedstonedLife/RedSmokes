package com.bss.inc.redsmokes.main.commands;

import com.bss.inc.redsmokes.main.CommandSource;
import com.bss.inc.redsmokes.main.textreader.SimpleTextInput;

import java.text.DateFormat;
import java.util.Calendar;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Commandbalancetop extends RedSmokesCommand {
    public static int MINUSERS = 50;
    private static int CACHETIME = 2 * 60 * 1000;
    private static SimpleTextInput cache = new SimpleTextInput();

    public Commandbalancetop() {
        super("balancetop");
    }

    private void outputCache(final CommandSource sender, final int page) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(redSmokes.getBalanceTop().getCacheAge());
        final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        sender.sendMessage(tl("balanceTop", format.format(cal.getTime())));
        new TextPager(cache).showPage(Integer.toString(page), null, "balancetop", sender);
    }
}
