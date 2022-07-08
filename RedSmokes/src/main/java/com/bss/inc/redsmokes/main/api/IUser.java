package com.bss.inc.redsmokes.main.api;

import com.bss.inc.redsmokes.api.commands.IrsCommand;

public interface IUser {
    boolean isAuthorized(String node);
    boolean isAuthorized(IrsCommand cmd);
    boolean isAuthorized(IrsCommand cmd, String permissionPrefix);
    boolean isPermissionSet(String node);
    /**
        RedSmokes Balance (IF ESSENTIALS is not found)
        Will be deprecated in the future to be included in <br>SoyuzCore</br>

        <h3>SoyuzCore</h3>
        SoyuzCore is a private project by <p href="https://github.com/RedstonedLife">Tal A. Baskin</p> as part of a
        
     */
}
