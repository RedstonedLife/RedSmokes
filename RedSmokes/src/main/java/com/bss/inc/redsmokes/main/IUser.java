package com.bss.inc.redsmokes.main;

import net.redsmokes.api.MaxMoneyException;
import net.redsmokes.api.commands.IrsCommand;
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
        RedSmokes Balance (IF RedSmokes is not found)
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
     * Will use RedSmokes API if present, If not will use RedSmokes Balance
     * @return amount of money
     */
    BigDecimal getMoney();
    boolean isBaltopExempt();
    void setMoney(final BigDecimal value) throws MaxMoneyException;
    String getGroup();
    boolean inGroup(final String group);
    void sendMessage(String message);
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
    Map<User, BigDecimal> getConfirmingPayments();
    /**
     * 'Hidden' Represents when a player is hidden from others. This status includes when the player is hidden via other
     * supported plugins. Use isVanished() if you want to check if a user is vanished by RedSmokes.
     *
     * @return If the user is hidden or not
     * @see IUser#isVanished()
     */
    boolean isHidden();
    void setHidden(boolean vanish);
    /**
     * 'Vanished' Represents when a player is hidden from others by RedSmokes. This status does NOT include when the
     * player is hidden via other plugins. Use isHidden() if you want to check if a user is vanished by any supported
     * plugin.
     *
     * @return If the user is vanished or not
     * @see IUser#isHidden()
     */
    boolean isVanished();
    void setVanished(boolean vanish);
    Block getTargetBlock(int maxDistance);

}
