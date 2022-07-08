package com.bss.inc.redsmokes.main.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;

public interface IUser {
    boolean isAuthorized(String node);
    boolean isAuthorized(IrsCommand cmd);
    boolean isAuthorized()
}
