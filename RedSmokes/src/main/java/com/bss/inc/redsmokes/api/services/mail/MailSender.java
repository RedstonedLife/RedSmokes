package com.bss.inc.redsmokes.api.services.mail;

import net.redsmokes.api.IUser;

import java.util.UUID;

/**
 * An entity which is allowed to send mail to an {@link IUser IUser}.
 *
 * In RedSmokes, IUser and Console are the entities that implement this interface.
 */
public interface MailSender {
    /**
     * Gets the username of this {@link MailSender}.
     * @return The sender's username.
     */
    String getName();

    /**
     * Gets the {@link UUID} of this {@link MailSender} or null if this sender doesn't have a UUID.
     * @return The sender's {@link UUID} or null if N/A.
     */
    UUID getUUID();
}
