package net.redsmokes.api;

import static com.bss.inc.redsmokes.main.I18n.tl;

/**
 * @deprecated You should use {@link com.bss.inc.redsmokes.main.api.NoLoanPermittedException} instead of this class.
 */
@Deprecated
public class NoLoanPermittedException extends Exception {
    public NoLoanPermittedException() {
        super(tl("negativeBalanceError"));
    }
}
