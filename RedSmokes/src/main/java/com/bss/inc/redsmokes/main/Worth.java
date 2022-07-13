package com.bss.inc.redsmokes.main;

import com.bss.inc.redsmokes.main.commands.NotEnoughArgumentsException;
import com.bss.inc.redsmokes.main.config.ConfigurateUtil;
import com.bss.inc.redsmokes.main.config.RedSmokesConfiguration;
import com.bss.inc.redsmokes.main.utils.VersionUtil;
import net.redsmokes.api.IRedSmokes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class Worth implements IConf {
    private final RedSmokesConfiguration config;

    public Worth(final File dataFolder) {
        config = new RedSmokesConfiguration(new File(dataFolder, "worth.yml"), "/worth.yml");
        config.load();
    }

    /**
     * Get the value of an item stack from the config.
     *
     * @param redSmokes       The Essentials instance.
     * @param itemStack The item stack to look up in the config.
     * @return The price from the config.
     */
    public BigDecimal getPrice(final IRedSmokes redSmokes, final ItemStack itemStack) {
        BigDecimal result = BigDecimal.ONE.negate();

        final String itemname = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");

        if (VersionUtil.PRE_FLATTENING) {
            // Check for matches with data value from stack
            // Note that we always default to BigDecimal.ONE.negate(), equivalent to -1
            result = config.getBigDecimal("worth." + itemname + "." + itemStack.getDurability(), BigDecimal.ONE.negate());
        }

        // Check for matches with data value 0
        if (result.signum() < 0) {
            final CommentedConfigurationNode itemNameMatch = config.getSection("worth." + itemname);
            if (itemNameMatch != null && ConfigurateUtil.getKeys(itemNameMatch).size() == 1) {
                result = config.getBigDecimal("worth." + itemname + ".0", BigDecimal.ONE.negate());
            }
        }

        // Check for matches with data value wildcard
        if (result.signum() < 0) {
            result = config.getBigDecimal("worth." + itemname + ".*", BigDecimal.ONE.negate());
        }

        // Check for matches with item name alone
        if (result.signum() < 0) {
            result = config.getBigDecimal("worth." + itemname, BigDecimal.ONE.negate());
        }

        if (result.signum() < 0) {
            return null;
        }
        return result;
    }

    /**
     * Get the amount of items to be sold from a player's inventory.
     *
     * @param redSmokes        The Essentials instance.
     * @param user       The user attempting to sell the item.
     * @param is         A stack of the item to search the inventory for.
     * @param args       The amount to try to sell.
     * @param isBulkSell Whether or not to try and bulk sell all items.
     * @return The amount of items to sell from the player's inventory.
     * @throws Exception Thrown if trying to sell air or an invalid amount.
     */
    public int getAmount(final IRedSmokes redSmokes, final User user, final ItemStack is, final String[] args, final boolean isBulkSell) throws Exception {
        if (is == null || is.getType() == Material.AIR) {
            throw new Exception(tl("itemSellAir"));
        }

        int amount = 0;

        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            } catch (final NumberFormatException ex) {
                throw new NotEnoughArgumentsException(ex);
            }
            if (args[1].startsWith("-")) {
                amount = -amount;
            }
        }

        final boolean stack = args.length > 1 && args[1].endsWith("s");
        final boolean requireStack = redSmokes.getSettings().isTradeInStacks(is.getType());

        if (requireStack && !stack) {
            throw new Exception(tl("itemMustBeStacked"));
        }

        int max = 0;
        for (final ItemStack s : user.getBase().getInventory().getContents()) {
            if (s == null || !s.isSimilar(is)) {
                continue;
            }
            max += s.getAmount();
        }

        if (stack) {
            amount *= is.getType().getMaxStackSize();
        }
        if (amount < 1) {
            amount += max;
        }

        if (requireStack) {
            amount -= amount % is.getType().getMaxStackSize();
        }
        if (amount > max || amount < 1) {
            if (!isBulkSell) {
                user.sendMessage(tl("itemNotEnough2"));
                user.sendMessage(tl("itemNotEnough3"));
                throw new Exception(tl("itemNotEnough1"));
            } else {
                return amount;
            }
        }

        return amount;
    }

    /**
     * Set the price of an item and save it to the config.
     *
     * @param redSmokes       The RedSmokes instance.
     * @param itemStack A stack of the item to save.
     * @param price     The new price of the item.
     */
    public void setPrice(final IRedSmokes redSmokes, final ItemStack itemStack, final double price) {
        String path = "worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");

        // Spigot 1.13+ throws an exception if a 1.13+ plugin even *attempts* to do set data.
        if (VersionUtil.PRE_FLATTENING && itemStack.getType().getData() == null) {
            // Bukkit-bug: getDurability still contains the correct value, while getData().getData() is 0.
            path = path + "." + itemStack.getDurability();
        }

        config.setProperty(path, price);
        config.save();
    }

    public File getFile() {
        return config.getFile();
    }

    @Override
    public void reloadConfig() {
        config.load();
    }
}
