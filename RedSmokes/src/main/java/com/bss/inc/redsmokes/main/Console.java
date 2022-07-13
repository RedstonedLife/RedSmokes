package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.messaging.IMessageRecipient;
import net.redsmokes.api.IRedSmokes;

import static com.bss.inc.redsmokes.main.I18n.tl;

public final class Console implements IMessageRecipient {
    public static final String NAME = "Console";
    public static final String DISPLAY_NAME = tl("consoleName");
    private static Console instance; // Set in RedSmokes

    private final IRedSmokes redSmokes;
    private final IMessageRecipient messageRecipient;

    private Console(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        
    }
}
