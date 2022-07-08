package com.bss.inc.redsmokes.main.provider;

import org.bukkit.Material;

public interface MaterialTagProvider {
    boolean tagExists(String tagName);

    boolean isTagged(String tagName, Material material);
}
