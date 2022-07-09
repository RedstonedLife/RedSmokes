package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public interface ISettings extends IConf {
    File getConfigFile();
    boolean areSignsDisabled();
    String getBackupCommand();
    long getBackupInterval();
    boolean isAlwaysRunBackup();
    BigDecimal getCommandCost(IrsCommand cmd);
    BigDecimal getCommandCost(String label);
    String getCurrencySymbol();
    boolean isCurrencySymbolSuffixed();
    String getLocale();
    BigDecimal getStartingBalance();
    boolean isCommandDisabled(final IrsCommand cmd);
    boolean isCommandDisabled(String label);
    Set<String> getDisabledCommands();
    boolean isVerboseCommandUsages();
    boolean isCommandOverriden(String name);
    boolean isDebug();
    void setDebug(boolean debug);
    boolean isEcoDisabled();
    List<RedSmokesSign> enabledSigns();
    BigDecimal getMaxMoney();
    BigDecimal getMinMoney();
    boolean isEcoLogEnabled();
    boolean isEcoLogUpdateEnabled();
    boolean allowUnsafeEnchantments();
    boolean getRepairEnchanted();
    int getSignUsePerSecond();
    int getMailsPerMinute();
    int getMaxUserCacheCount();
    boolean isNotifyNoNewMail();
    BigDecimal getMinimumPayAmount();
    long getCommandCooldownMs(String label);
    Entry<Pattern, Long> getCommandCooldownEntry(String label);
    boolean isCommandCooldownPersistent(String label);
    boolean isNpcsInBalanceRanking();
    NumberFormat getCurrencyFormat();
    List<RedSmokesSign> getUnprotectedSignNames();
    int getNotifyPlayerOfMailCooldown();
    
}
