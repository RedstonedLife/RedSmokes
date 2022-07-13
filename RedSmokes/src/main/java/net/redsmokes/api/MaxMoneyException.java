package net.redsmokes.api;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class MaxMoneyException extends Exception {
    public MaxMoneyException() {
        super(tl("maxMoney"));
    }
}
