package com.bss.inc.redsmokes.main;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class I18n implements com.bss.inc.redsmokes.api.II18n {
    private static final String MESSAGES = "messages";
    private static final Pattern NODOUBLEMARK = Pattern.compile("''");
    private static final ResourceBundle NULL_BUNDLE = new ResourceBundle() {
        @Override protected Object handleGetObject(final String key) {return null;}
        @Override public Enumeration<String> getKeys() {return null;}
    };
    private static I18n instance;
    private final transient Locale defaultLocale = Locale.getDefault();
    private final transient 
}
