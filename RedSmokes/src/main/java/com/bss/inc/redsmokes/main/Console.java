package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.messaging.IMessageRecipient;
import com.bss.inc.redsmokes.main.messaging.SimpleMessageRecipient;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    @Override
    public void sendMessage(final String message) {
        getCommandSender().sendMessage(message);
    }

    @Override
    public boolean isReachable() {
        return true;
    }

    /* ================================
     * >> DELEGATE METHODS
     * ================================ */

    @Override
    public MessageResponse sendMessage(final IMessageRecipient recipient, final String message) {
        return this.messageRecipient.sendMessage(recipient, message);
    }

    @Override
    public MessageResponse onReceiveMessage(final IMessageRecipient sender, final String message) {
        return this.messageRecipient.onReceiveMessage(sender, message);
    }

    @Override
    public IMessageRecipient getReplyRecipient() {
        return this.messageRecipient.getReplyRecipient();
    }

    @Override
    public void setReplyRecipient(final IMessageRecipient recipient) {
        this.messageRecipient.setReplyRecipient(recipient);
    }

    @Override
    public boolean isHiddenFrom(Player player) {
        return false;
    }
}
