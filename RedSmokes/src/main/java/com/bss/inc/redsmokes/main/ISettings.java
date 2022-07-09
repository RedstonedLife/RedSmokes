package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;

import java.io.File;

public interface ISettings extends IConf {
    File getConfigFile();
    String getBackupCommand();
    long getBackupInterval();
    
}
