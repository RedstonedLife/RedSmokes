package com.bss.inc.redsmokes.main.items;

import com.bss.inc.redsmokes.api.IConf;
import com.bss.inc.redsmokes.api.IItemDb;
import com.bss.inc.redsmokes.main.RedSmokes;
import com.bss.inc.redsmokes.main.config.ConfigurateUtil;
import com.bss.inc.redsmokes.main.config.RedSmokesConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemResolver implements IItemDb.ItemResolver, IConf {
    private final RedSmokesConfiguration config;
    private final RedSmokes redSmokes;
    private final HashMap<String, String> map = new HashMap<>();

    public CustomItemResolver(final RedSmokes redSmokes) {
        config = new RedSmokesConfiguration(new File(redSmokes.getDataFolder(), "custom_items.yml"), "/custom_items.yml");
        this.redSmokes = redSmokes;
    }

    @Override
    public ItemStack apply(String item) {
        item = item.toLowerCase();
        if (map.containsKey(item)) {
            try {
                return redSmokes.getItemDb().get(map.get(item));
            } catch (final Exception ignored) {
            }
        }

        return null;
    }

    @Override
    public Collection<String> getNames() {
        return map.keySet();
    }

    public List<String> getAliasesFor(String item) throws Exception {
        final List<String> results = new ArrayList<>();
        if (item != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (item.equalsIgnoreCase(redSmokes.getItemDb().name(redSmokes.getItemDb().get(entry.getValue())))) {
                    results.add(entry.getKey());
                }
            }
        }
        return results;
    }

    @Override
    public void reloadConfig() {
        map.clear();
        config.load();

        final Map<String, Object> section = ConfigurateUtil.getRawMap(config.getSection("aliases"));
        if (section.isEmpty()) {
            redSmokes.getLogger().warning("No aliases found in custom_items.yml.");
            return;
        }

        for (final Map.Entry<String, Object> entry : section.entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                continue;
            }
            final String alias = entry.getKey().toLowerCase();
            final String target = (String) entry.getValue();

            if (existsInItemDb(target)) {
                map.put(alias, target);
            }
        }
    }

    public void setAlias(String alias, final String target) {
        alias = alias.toLowerCase();
        if (map.containsKey(alias) && map.get(alias).equalsIgnoreCase(target)) {
            return;
        }

        map.put(alias, target);
        save();
    }

    public void removeAlias(final String alias) {
        if (map.remove(alias.toLowerCase()) != null) {
            save();
        }
    }

    private void save() {
        config.setRaw("aliases", map);
        config.save();
    }

    private boolean existsInItemDb(final String item) {
        try {
            redSmokes.getItemDb().get(item);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}
