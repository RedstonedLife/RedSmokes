package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.commands.IrsCommand;
import com.bss.inc.redsmokes.main.signs.RedSmokesSign;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
}
