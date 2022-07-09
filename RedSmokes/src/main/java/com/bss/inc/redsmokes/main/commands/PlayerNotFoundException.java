package com.bss.inc.redsmokes.main.commands;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class PlayerNotFoundException extends NoSuchFieldException {
    public PlayerNotFoundException() {
        super(tl("playerNotFound"));
    }
}
