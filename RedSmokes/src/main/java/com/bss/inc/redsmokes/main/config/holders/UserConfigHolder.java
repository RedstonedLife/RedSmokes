package com.bss.inc.redsmokes.main.config.holders;

import com.bss.inc.redsmokes.main.config.annotations.DeleteIfIncomplete;
import com.bss.inc.redsmokes.main.config.annotations.DeleteOnEmpty;
import com.bss.inc.redsmokes.main.config.entities.CommandCooldown;
import com.bss.inc.redsmokes.main.config.entities.LazyLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ConfigSerializable
public class UserConfigHolder {
    private @MonotonicNonNull BigDecimal money;

    public BigDecimal money() {
        return money;
    }

    public void money(final BigDecimal value) {
        this.money = value;
    }

    @DeleteOnEmpty
    private @MonotonicNonNull Map<String, LazyLocation> homes;

    public Map<String, LazyLocation> homes() {
        if (this.homes == null) {
            this.homes = new HashMap<>();
        }
        return this.homes;
    }

    public void homes(final Map<String, LazyLocation> value) {
        this.homes = value;
    }

    private @Nullable String nickname;

    public String nickname() {
        return nickname;
    }

    public void nickname(final String value) {
        this.nickname = value;
    }

    @DeleteOnEmpty
    private @MonotonicNonNull Set<Material> unlimited;

    public Set<Material> unlimited() {
        if (this.unlimited == null) {
            this.unlimited = new HashSet<>();
        }
        return this.unlimited;
    }

    @DeleteOnEmpty
    private @MonotonicNonNull Map<String, List<String>> powertools;

    public Map<String, List<String>> powertools() {
        if (this.powertools == null) {
            this.powertools = new HashMap<>();
        }
        return this.powertools;
    }

    private @MonotonicNonNull LazyLocation lastlocation;

    public LazyLocation lastLocation() {
        return this.lastlocation;
    }

    public void lastLocation(final Location value) {
        if (value == null || value.getWorld() == null) {
            return;
        }
        this.lastlocation = LazyLocation.fromLocation(value);
    }

    private @MonotonicNonNull LazyLocation logoutlocation;

    public LazyLocation logoutLocation() {
        return this.logoutlocation;
    }

    public void logoutLocation(final Location value) {
        if (value == null || value.getWorld() == null) {
            return;
        }
        this.logoutlocation = LazyLocation.fromLocation(value);
    }

    private boolean npc = false;

    public boolean npc() {
        return this.npc;
    }

    public void npc(final boolean value) {
        this.npc = value;
    }

    private @MonotonicNonNull String lastAccountName;

    public String lastAccountName() {
        return this.lastAccountName;
    }

    public void lastAccountName(final String value) {
        this.lastAccountName = value;
    }

    private boolean acceptingPay = true;

    public boolean acceptingPay() {
        return this.acceptingPay;
    }

    public void acceptingPay(final boolean value) {
        this.acceptingPay = value;
    }

    private @Nullable Boolean confirmPay;

    public Boolean confirmPay() {
        return this.confirmPay;
    }

    public void confirmPay(final Boolean value) {
        this.confirmPay = value;
    }

    private boolean baltopExempt = false;

    public boolean baltopExempt() {
        return this.baltopExempt;
    }

    public void baltopExempt(final boolean value) {
        this.baltopExempt = value;
    }

    private @NonNull Timestamps timestamps = new Timestamps();

    public Timestamps timestamps() {
        return this.timestamps;
    }

    @ConfigSerializable
    public static class Timestamps {
        @DeleteOnEmpty
        @DeleteIfIncomplete
        private @MonotonicNonNull List<CommandCooldown> commandCooldowns;

        public List<CommandCooldown> commandCooldowns() {
            if (this.commandCooldowns == null) {
                this.commandCooldowns = new ArrayList<>();
            }
            return this.commandCooldowns;
        }

        public void commandCooldowns(final List<CommandCooldown> value) {
            this.commandCooldowns = value;
        }
    }
}
