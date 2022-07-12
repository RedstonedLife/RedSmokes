package com.bss.inc.redsmokes.main.commands;

public class QuietAbortException extends Exception {
    public QuietAbortException() {
        super();
    }

    public QuietAbortException(final String message) {
        super(message);
    }
}
