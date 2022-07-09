package com.bss.inc.redsmokes.main.textreader;

import java.util.List;

public interface IText {
    // Contains the raw text lines
    List<String> getLines();

    // Chapters contain the names that are displayed automatically if the file doesn't contain a introduction chapter.
    List<String> getChapters();

    // Bookmarks contains the string mappings from 'chapters' to line numbers.
}
