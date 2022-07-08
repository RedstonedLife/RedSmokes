package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.ProviderListener;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.function.Consumer;

public class PaperRecipeBookListener extends ProviderListener {
    public PaperRecipeBookListener(final Consumer<Event> function) {
        super(function);
    }

    @EventHandler
    public void onPlayerRecipeBookClick(final PlayerRecipeBookClickEvent event) {
        function.accept(event);
    }

    @Override
    public String getDescription() {
        return "Paper Player Recipe Book Click Event Provider";
    }
}
