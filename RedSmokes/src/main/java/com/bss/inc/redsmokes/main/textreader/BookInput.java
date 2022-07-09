package com.bss.inc.redsmokes.main.textreader;

import com.bss.inc.redsmokes.api.IRedSmokes;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;

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
            if(createFile) {
                try (final InputStream input = redSmokes.getResource(filename + ".txt"); final OutputStream output = new FileOutputStream(file)) {
                    final byte[] buffer = new byte[1024];
                    int length = input.read(buffer);
                    while (length > 0) {
                        output.write(buffer, 0, length);
                        length = input.read(buffer);
                    }
                }
                redSmokes.getLogger().info("File " + filename + ".txt does not exist. Creating one for you");
            }
        }
        if(!file.exists()) {
            lastChange = 0;
            lines = Collections.emptyList();
            chapters = Collections.emptyList();
            bookmarks = Collections.emptyMap();
            throw new FileNotFoundException("Could not create " + filename + ".txt");
        } else {
            lastChange = file.lastModified();
            final boolean readFromfile;
            synchronized (cache) {
                final SoftReference<BookInput> inputRef = cache.get(file.getName());
                final BookInput input;
                if(inputRef == null || (input = inputRef.get()) == null || input.lastChange < lastChange) {
                    lines = new ArrayList<>();
                    chapters = new ArrayList<>();
                    bookmarks = new HashMap<>();
                    cache.put(file.getName(), new SoftReference<>(this));
                    readFromfile = true;
                } else {
                    
                }
            }
        }
    }
}
