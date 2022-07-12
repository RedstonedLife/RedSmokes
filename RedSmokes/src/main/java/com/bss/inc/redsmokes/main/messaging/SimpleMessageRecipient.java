package com.bss.inc.redsmokes.main.messaging;

import com.bss.inc.redsmokes.main.User;
import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.IUser;
import net.redsmokes.api.events.PrivateMessagePreSendEvent;
import net.redsmokes.api.events.PrivateMessageSentEvent;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static com.bss.inc.redsmokes.main.I18n.tl;


/**
 * Represents a simple reusable implementation of {@link IMessageRecipient}. This class provides functionality for the following methods:
 * <ul>
 *     <li>{@link IMessageRecipient#sendMessage(IMessageRecipient, String)}</li>
 *     <li>{@link IMessageRecipient#onReceiveMessage(IMessageRecipient, String)}</li>
 *     <li>{@link IMessageRecipient#getReplyRecipient()}</li>
 *     <li>{@link IMessageRecipient#setReplyRecipient(IMessageRecipient)}</li>
 * </ul>
 *
 * <b>The given {@code parent} must implement the following methods to prevent overflow:</b>
 * <ul>
 *     <li>{@link IMessageRecipient#sendMessage(String)}</li>
 *     <li>{@link IMessageRecipient#getName()}</li>
 *     <li>{@link IMessageRecipient#getDisplayName()}</li>
 *     <li>{@link IMessageRecipient#isReachable()}</li>
 * </ul>
 * <p>
 * The reply-recipient is wrapped in a {@link WeakReference}.
 */
public class SimpleMessageRecipient implements IMessageRecipient {

    private final IRedSmokes redSmokes;
    private final IMessageRecipient parent;

    private long lastMessageMs;
    private WeakReference<IMessageRecipient> replyRecipient;

    public SimpleMessageRecipient(final IRedSmokes redSmokes, final IMessageRecipient parent) {
        this.redSmokes = redSmokes;
        this.parent = parent;
    }

    protected static User getUser(final IMessageRecipient recipient) {
        if (recipient instanceof SimpleMessageRecipient) {
            return ((SimpleMessageRecipient) recipient).parent instanceof User ? (User) ((SimpleMessageRecipient) recipient).parent : null;
        }
        return recipient instanceof User ? (User) recipient : null;
    }

    @Override
    public void sendMessage(final String message) {
        this.parent.sendMessage(message);
    }

    @Override
    public String getName() {
        return this.parent.getName();
    }

    @Override
    public UUID getUUID() {
        return this.parent.getUUID();
    }

    @Override
    public String getDisplayName() {
        return this.parent.getDisplayName();
    }

    @Override
    public MessageResponse sendMessage(final IMessageRecipient recipient, String message) {
        final PrivateMessagePreSendEvent preSendEvent = new PrivateMessagePreSendEvent(parent, recipient, message);
        redSmokes.getServer().getPluginManager().callEvent(preSendEvent);
        if (preSendEvent.isCancelled()) {
            return MessageResponse.EVENT_CANCELLED;
        }

        message = preSendEvent.getMessage();
        final MessageResponse messageResponse = recipient.onReceiveMessage(this.parent, message);
        switch (messageResponse) {
            case UNREACHABLE:
                sendMessage(tl("recentlyForeverAlone", recipient.getDisplayName()));
                break;
            case MESSAGES_IGNORED:
                sendMessage(tl("msgIgnore", recipient.getDisplayName()));
                break;
            case SENDER_IGNORED:
                break;
            // When this recipient is AFK, notify the sender. Then, proceed to send the message.
                // fall through
            default:
                sendMessage(tl("msgFormat", tl("meSender"), recipient.getDisplayName(), message));

                // Better Social Spy
                break;
        }
        // If the message was a success, set this sender's reply-recipient to the current recipient.
        if (messageResponse.isSuccess()) {
            setReplyRecipient(recipient);
        }

        final PrivateMessageSentEvent sentEvent = new PrivateMessageSentEvent(parent, recipient, message, messageResponse);
        redSmokes.getServer().getPluginManager().callEvent(sentEvent);

        return messageResponse;
    }

    @Override
    public MessageResponse onReceiveMessage(final IMessageRecipient sender, final String message) {
        if (!isReachable()) {
            return MessageResponse.UNREACHABLE;
        }

        final User user = getUser(this);
        // Display the formatted message to this recipient.
        sendMessage(tl("msgFormat", sender.getDisplayName(), tl("meRecipient"), message));

        this.lastMessageMs = System.currentTimeMillis();
        return MessageResponse.SUCCESS;
    }

    @Override
    public boolean isReachable() {
        return this.parent.isReachable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>This {@link com.bss.inc.redsmokes.main.messaging.SimpleMessageRecipient} implementation stores the a weak reference to the recipient.</b>
     */
    @Override
    public IMessageRecipient getReplyRecipient() {
        return replyRecipient == null ? null : replyRecipient.get();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>This {@link com.bss.inc.redsmokes.main.messaging.SimpleMessageRecipient} implementation stores the a weak reference to the recipient.</b>
     */
    @Override
    public void setReplyRecipient(final IMessageRecipient replyRecipient) {
        this.replyRecipient = new WeakReference<>(replyRecipient);
    }

    @Override
    public boolean isHiddenFrom(Player player) {
        return parent.isHiddenFrom(player);
    }
}
