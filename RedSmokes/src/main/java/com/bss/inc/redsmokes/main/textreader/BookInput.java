package com.bss.inc.redsmokes.main.textreader;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class BookInput implements IText {
    private final static HashMap<String, SoftReference<BookInput>> cache = new HashMap<>();
    private final transient List<String> lines;
    private final transient List<String> chapters;
    private final transient Map<String, Integer>
}
