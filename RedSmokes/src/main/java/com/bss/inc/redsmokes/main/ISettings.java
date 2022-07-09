package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.commands.IrsCommand;

import java.io.File;
import java.math.BigDecimal;

public interface ISettings extends IConf {
    File getConfigFile();
    String getBackupCommand();
    long getBackupInterval();
    boolean isAlwaysRunBackup();
    BigDecimal getCommandCost(IrsCommand cmd);
    BigDecimal getCommandCost(String label);
    
}
