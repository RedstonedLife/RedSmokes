package com.bss.inc.redsmokes.main.perm.impl;

import com.bss.inc.redsmokes.main.RedSmokes;
import com.bss.inc.redsmokes.main.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class LuckPermsHandler extends ModernVaultHandler {
    private LuckPerms luckPerms;
    private RedSmokes redSmokes;
    private CombinedCalculator calculator;

    @Override
    public void registerContext(final String context, final Function<User, Iterable<String>> calculator, final Supplier<Iterable<String>> suggestions) {
        if (this.calculator == null) {
            this.calculator = new CombinedCalculator();
            this.luckPerms.getContextManager().registerCalculator(this.calculator);
        }
        this.calculator.calculators.add(new Calculator(context, calculator, suggestions));
    }

    @Override
    public void unregisterContexts() {
        if (this.calculator != null) {
            this.luckPerms.getContextManager().unregisterCalculator(this.calculator);
            this.calculator = null;
        }
    }

    @Override
    public boolean tryProvider(RedSmokes redSmokes) {
        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            this.redSmokes = redSmokes;
        }
        return luckPerms != null && super.tryProvider(redSmokes);
    }

    private static final class Calculator {
        private final String id;
        private final Function<User, Iterable<String>> function;
        private final Supplier<Iterable<String>> suggestions;

        private Calculator(String id, Function<User, Iterable<String>> function, Supplier<Iterable<String>> suggestions) {
            this.id = id;
            this.function = function;
            this.suggestions = suggestions;
        }
    }

    // By combining all calculators into one, we only need to make one call to ess.getUser().
    private class CombinedCalculator implements ContextCalculator<Player> {
        private final Set<Calculator> calculators = new HashSet<>();

        @Override
        public void calculate(final Player target, final ContextConsumer consumer) {
            // If the player doesn't exist in the UserMap, just skip
            // Ess will cause performance problems for permissions checks if it attempts to
            // perform i/o to load the user data otherwise.
            if (!redSmokes.getUserMap().userExists(target.getUniqueId())) {
                return;
            }

            final User user = redSmokes.getUser(target);
            for (Calculator calculator : this.calculators) {
                calculator.function.apply(user).forEach(value -> consumer.accept(calculator.id, value));
            }
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            for (Calculator calculator : this.calculators) {
                calculator.suggestions.get().forEach(value -> builder.add(calculator.id, value));
            }

            return builder.build();
        }
    }
}
