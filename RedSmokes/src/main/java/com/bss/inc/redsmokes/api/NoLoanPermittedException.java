package com.bss.inc.redsmokes.api;

/**
 * @deprecated You should use {@link com.bss.inc.redsmokes.main.} instead of this class.
 */
@Deprecated
public class NoLoanPermittedException extends Exception {
    public NoLoanPermittedException() {
        super(tl("negativeBalanceError"));
    }
}
