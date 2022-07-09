package com.bss.inc.redsmokes.main.perm.impl;

import com.bss.inc.redsmokes.main.RedSmokes;

public class GenericVaultHandler extends AbstractVaultHandler {
    @Override
    public boolean tryProvider(RedSmokes ess) {
        return super.canLoad();
    }
}
