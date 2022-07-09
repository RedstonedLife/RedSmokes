package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.MaxMoneyException;
import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.api.services.mail.MailMessage;
import com.bss.inc.redsmokes.api.services.mail.MailSender;
import com.bss.inc.redsmokes.main.config.entities.CommandCooldown;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public interface IUser {
    boolean isAuthorized(String node);
    boolean isAuthorized(IrsCommand cmd);
    boolean isAuthorized(IrsCommand cmd, String permissionPrefix);
    boolean isPermissionSet(String node);
    /**
        RedSmokes Balance (IF ESSENTIALS is not found)
        Will be deprecated in the future included in <br>SoyuzCore</br> and will be imported from its API.

        <h3>SoyuzCore</h3>
        SoyuzCore is a private project by <p href="https://github.com/RedstonedLife">Tal A. Baskin</p> as part of a
        RP-CMP meant to simulate a geopolitical world with established countries with constitutions, laws, etc....
     */
    void giveMoney(BigDecimal value) throws MaxMoneyException;
    void giveMoney(final BigDecimal value, final CommandSource initiator) throws MaxMoneyException;
    void payUser(final User receiver, final BigDecimal value) throws Exception;
    void takeMoney(BigDecimal value);
    void takeMoney(final BigDecimal value, final CommandSource initiator);
    boolean canAfford(BigDecimal value);

    /**
     * Will use Essentials API if present, If not will use RedSmokes Balance
     * @return amount of money
     */
    BigDecimal getMoney();
    void setMoney(final BigDecimal value) throws MaxMoneyException;
    String getGroup();
    boolean inGroup(final String group);
    void sendMessage(String message);
    void sendMail(MailSender sender, String message);
    void sendMail(MailSender sender, String message, long expireAt);
    ArrayList<MailMessage> getMailMessages();
    void setMailList(ArrayList<MailMessage> messages);
    int getMailAmount();
    Set<String> getConfigKeys();
    Map<String, Object> getConfigMap();
    Map<String, Object> getConfigMap(String node);
    List<CommandCooldown> getCooldownsList();
    Date getCommandCooldownExpiry(String label);
    void addCommandCooldown(Pattern pattern, Date expiresAt, boolean save);
    boolean clearCommandCooldown(Pattern pattern);

    /*
     * PlayerExtension
     */
    Player getBase();
    CommandSource getSource();
    String getName();
    UUID getUUID();
    String getDisplayName();
    boolean isAcceptingPay();
    void setAcceptingPay(boolean acceptingPay);
    boolean isPromptingPayConfirm();
    void setPromptingPayConfirm(boolean prompt);
    boolean isPromptingClearConfirm();
    void setPromptingClearConfirm(boolean prompt);
}
