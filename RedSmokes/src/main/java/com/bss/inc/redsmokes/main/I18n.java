package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.api.IRedSmokes;

import java.text.MessageFormat;
import java.util.*;
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
    private final transient ResourceBundle defaultBundle;
    private final transient IRedSmokes redSmokes;
    private transient Locale currentLocale = defaultLocale;
    private transient ResourceBundle customBundle;
    private transient ResourceBundle localeBundle;
    private transient Map<String, MessageFormat> messageFormatCache = new HashMap<>();

    public I18n(final IRedSmokes redSmokes) {
        this.redSmokes = redSmokes;
        defaultBundle = ResourceBundle.getBundle(MESSAGES, Locale.ENGLISH, new UTF8PropertiesControl());
        localeBundle = defaultBundle;
        customBundle = NULL_BUNDLE;
    }

    public static String tl(final String string, final Object... objects) {
        if(instance==null){return "";}
        if(objects.length==0) {
            return NODOUBLEMARK.matcher(instance.translate(string)).replaceAll("'");
        } else {
            return instance.format(string, objects);
        }
    }
}
