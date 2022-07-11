package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.IRedSmokes;
import com.bss.inc.redsmokes.main.config.RedSmokesUserConfiguration;

public abstract class UserData extends PlayerExtension implements IConf {
    protected final transient IRedSmokes redsmokes;
    private final RedSmokesUserConfiguration config;
}
