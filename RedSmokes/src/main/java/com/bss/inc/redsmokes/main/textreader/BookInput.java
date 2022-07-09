package com.bss.inc.redsmokes.main.textreader;

import com.bss.inc.redsmokes.api.IRedSmokes;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookInput implements IText {
    private final static HashMap<String, SoftReference<BookInput>> cache = new HashMap<>();
    private final transient List<String> lines;
    private final transient List<String> chapters;
    private final transient Map<String, Integer> bookmarks;
    private final transient long lastChange;

    public BookInput(final String filename, final boolean createFile, final IRedSmokes redSmokes) throws IOException {
        File file = null;
        if(file == null || !file.exists()) {
            file = new File(redSmokes.getDataFolder(), filename + ".txt");
        }
        if(!file.exists()) {
            
        }
    }
}
