package com.bss.inc.redsmokes.main.commands;

public class NoChargeException extends Exception {
    public NoChargeException() {
        super("Will charge later");
    }
}
