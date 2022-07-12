package com.bss.inc.redsmokes.main.commands;

public class NotEnoughArgumentsException extends Exception {
    public NotEnoughArgumentsException() {
        super("");
    }

    public NotEnoughArgumentsException(final String string) {
        super(string);
    }

    public NotEnoughArgumentsException(final Throwable ex) {
        super("", ex);
    }
}
