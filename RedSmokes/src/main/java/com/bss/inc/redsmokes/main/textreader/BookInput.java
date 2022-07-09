package com.bss.inc.redsmokes.main.textreader;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class BookInput implements IText {
    private final static HashMap<String, SoftReference<BookInput>> cache = new HashMap<>();
    private final transient List<String> lines;
    
}
