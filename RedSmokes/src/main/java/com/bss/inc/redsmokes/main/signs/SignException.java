package com.bss.inc.redsmokes.main.signs;

public class SignException extends Exception {
    public SignException(final String message) {
        super(message);
    }

    public SignException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
