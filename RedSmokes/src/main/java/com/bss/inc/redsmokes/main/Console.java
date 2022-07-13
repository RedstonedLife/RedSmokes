package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.messaging.IMessageRecipient;
import com.bss.inc.redsmokes.main.messaging.SimpleMessageRecipient;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.util.UUID;

import static com.bss.inc.redsmokes.main.I18n.tl;

public final class Console implements IMessageRecipient {
    public static final String NAME = "Console";
    public static final String DISPLAY_NAME = tl("consoleName");
    private static Console instance; // Set in RedSmokes

    private final IRedSmokes redSmokes;
    private final IMessageRecipient messageRecipient;

    private Console(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        this.messageRecipient = new SimpleMessageRecipient(redSmokes,this);
    }
    public static Console getInstance() {
        return instance;
    }

    static void setInstance(final IRedSmokes redSmokes) { // Called in RedSmokes#onEnable()
        instance = new Console(redSmokes);
    }

    /**
     * @deprecated Use {@link Console#getCommandSender()}
     */
    @Deprecated
    public static CommandSender getCommandSender(final Server server) throws Exception {
        return server.getConsoleSender();
    }

    public CommandSender getCommandSender() {
        return redSmokes.getServer().getConsoleSender();
    }

    @Override
    public String getName() {
        return Console.NAME;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return Console.DISPLAY_NAME;
    }

    @
}
