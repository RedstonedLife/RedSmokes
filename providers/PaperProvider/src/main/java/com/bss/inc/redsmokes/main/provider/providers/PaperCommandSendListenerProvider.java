package com.bss.inc.redsmokes.main.provider.providers;

import com.bss.inc.redsmokes.main.provider.CommandSendListenerProvider;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Collection;
import java.util.function.Predicate;

public class PaperCommandSendListenerProvider extends CommandSendListenerProvider {

    public PaperCommandSendListenerProvider(Filter commandFilter) {
        super(commandFilter);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncCommandSend(@SuppressWarnings("deprecation") AsyncPlayerSendCommandsEvent<?> event) {
        if (!event.isAsynchronous() && event.hasFiredAsync()) {
            // this has already fired once async
            return;
        }

        final Collection<? extends CommandNode<?>> children = event.getCommandNode().getChildren();
        final Predicate<String> filter = filter(event.getPlayer());

        children.removeIf(node -> filter.test(node.getName()));
    }

    @Override
    public String getDescription() {
        return "Paper async Brigadier command send listener";
    }
}
