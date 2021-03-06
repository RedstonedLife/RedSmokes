package com.bss.inc.redsmokes.main.signs;

import net.redsmokes.api.IRedSmokes;
import net.redsmokes.api.MaxMoneyException;
import net.redsmokes.api.events.SignBreakEvent;
import net.redsmokes.api.events.SignCreateEvent;
import net.redsmokes.api.events.SignInteractEvent;
import com.bss.inc.redsmokes.main.*;
import com.bss.inc.redsmokes.main.utils.FormatUtil;
import com.bss.inc.redsmokes.main.utils.MaterialUtil;
import com.bss.inc.redsmokes.main.utils.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.bss.inc.redsmokes.main.I18n.tl;

public class RedSmokesSign {
    private static final String SIGN_OWNER_KEY = "sign-owner";
    protected static final BigDecimal MINTRANSACTION = new BigDecimal("0.01");
    private static final Set<Material> EMPTY_SET = new HashSet<>();
    protected transient final String signName;

    public RedSmokesSign(final String signName) {
        this.signName = signName;
    }

    protected static boolean checkIfBlockBreaksSigns(final Block block) {
        final Block sign = block.getRelative(BlockFace.UP);
        if(MaterialUtil.isSignPost(sign.getType()) && isValidSign(new BlockSign(sign))) {return true;}
        final BlockFace[] directions = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for(final BlockFace blockFace : directions) {
            final Block signBlock = block.getRelative(blockFace);
            if(MaterialUtil.isWallSign(signBlock.getType())) {
                try {
                    if(getWallSignFacing(signBlock) == blockFace && isValidSign(new BlockSign(signBlock))) {
                        return true;
                    }
                } catch(final NullPointerException ex) {
                    // Sometimes signs enter a state of being semi broken, having no text or state data, usually while burning
                }
            }
        }
        return false;
    }

    /**
     * @deprecated use {@link #isValidSign(IRedSmokes, ISign)} if possible
     * @param sign
     * @return boolean True if valid, false if invalid
     */
    @Deprecated
    public static boolean isValidSign(final ISign sign) {return sign.getLine(0).matches("??1\\[.*]");}

    public static boolean isValidSign(final IRedSmokes redSmokes, final ISign sign) {
        if(!sign.getLine(0).matches("??1\\[.*]"))
            return false;
        // Validate that the sign is actually an RedSmokes sign
        final String signName = ChatColor.stripColor(sign.getLine(0)).replaceAll("[^a-zA-Z]", "");
        for(final RedSmokesSign redSign : redSmokes.getSettings().enabledSigns()) {
            if(redSign.getName().equalsIgnoreCase(signName))
                return true;
        }
        return false;
    }

    private static BlockFace getWallSignFacing(final Block block) {
        try {
            final WallSign signData = (WallSign) block.getState().getBlockData();
            return signData.getFacing();
        } catch (final NoClassDefFoundError | NoSuchMethodError e) {
            final org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) block.getState().getData();
            return signMat.getFacing();
        }
    }

    protected final boolean onSignCreate(final SignChangeEvent event, final IRedSmokes redSmokes) {
        final ISign sign = new EventSign(event);
        final User user = redSmokes.getUser(event.getPlayer());
        if(!(user.isAuthorized("redsmokes.signs."+signName.toLowerCase(Locale.ENGLISH)+".create") || user.isAuthorized("essentials.signs.create." + signName.toLowerCase(Locale.ENGLISH)))) {
            // Return true, so other plugins can use the same sign title, just hope
            // they won't change it to ??1[Signname]
            return true;
        }
        sign.setLine(0, tl("signFormatFail", this.signName));

        final SignCreateEvent signEvent = new SignCreateEvent(sign, this, user);
        redSmokes.getServer().getPluginManager().callEvent(signEvent);
        if(signEvent.isCancelled()) {
            if(redSmokes.getSettings().isDebug()) {
                redSmokes.getLogger().info("SignCreateEvent called for sign " + signEvent.getRedSign().getName());
            }
            return false;
        }

        try {
            final boolean ret = onSignCreate(sign, user, getUsername(user), redSmokes);
            if(ret) {
                sign.setLine(0, getSuccessName(redSmokes));
            }
            return ret;
        } catch (final ChargeException | SignException ex) {
            showError(redSmokes, user.getSource(), ex, signName);
        }
        setOwnerData(redSmokes, user, sign);
        // Return true, so the player sees the wrong sign
        return true;
    }

    public String getSuccessName(final IRedSmokes redSmokes) {
        final String successName = getSuccessName();
        if(successName == null) {
            redSmokes.getLogger().severe("signFormatSuccess message must use the {0} argument.");
        }
        return successName;
    }

    public String getSuccessName() {
        String successName = tl("signFormatSuccess", this.signName);
        if(successName.isEmpty() || !successName.contains(this.signName)) {
            // Set to null to cause an error in place of no functionality. This makes an error obvious as opposed to leaving users baffled by lack of
            // functionality
            successName = null;
        }
        return successName;
    }

    public String getTemplateName() {
        return tl("signFormatTemplate", this.signName);
    }

    public String getName() {
        return this.signName;
    }

    public String getUsername(final User user) {
        // Truncate username to ensure it can fit on a sign
        return user.getName().substring(0, Math.min(user.getName().length(), 13));
    }

    public void setOwner(final IRedSmokes redSmokes, final User user, final ISign signProvider, final int nameIndex, final String namePrefix) {
        setOwnerData(redSmokes, user, signProvider);
        signProvider.setLine(nameIndex, namePrefix + getUsername(user));
    }

    public void setOwnerData(final IRedSmokes redSmokes, final User user, final ISign signProvider) {
        if(redSmokes.getSignDataProvider() == null) {
            return;
        }
        final Sign sign = (Sign) signProvider.getBlock().getState();
        redSmokes.getSignDataProvider().setSignData(sign, SIGN_OWNER_KEY, user.getUUID().toString());
    }

    public boolean isOwner(final IRedSmokes redSmokes, final User user, final ISign signProvider, final int nameIndex, final String namePrefix) {
        final Sign sign = (Sign) signProvider.getBlock().getState();
        if(redSmokes.getSignDataProvider() == null || redSmokes.getSignDataProvider().getSignData(sign, SIGN_OWNER_KEY) == null) {
            final boolean isLegacyOwner = FormatUtil.stripFormat(signProvider.getLine(nameIndex)).equalsIgnoreCase(getUsername(user));
            if(redSmokes.getSignDataProvider() != null && isLegacyOwner) {
                redSmokes.getSignDataProvider().setSignData(sign, SIGN_OWNER_KEY, user.getUUID().toString());
            }
            return isLegacyOwner;
        }

        if(user.getUUID().toString().equals(redSmokes.getSignDataProvider().getSignData(sign, SIGN_OWNER_KEY))) {
            signProvider.setLine(nameIndex, namePrefix + getUsername(user));
            return true;
        }
        return false;
    }

    protected final boolean onSignInteract(final Block block, final Player player, final IRedSmokes redSmokes) {
        final ISign sign = new BlockSign(block);
        final User user = redSmokes.getUser(player);
        if (user.checkSignThrottle()) {
            return false;
        }
        try {
            if (user.getBase().isDead() || !(user.isAuthorized("redsmokes.signs." + signName.toLowerCase(Locale.ENGLISH) + ".use") || user.isAuthorized("redsmokes.signs.use." + signName.toLowerCase(Locale.ENGLISH)))) {
                return false;
            }

            final SignInteractEvent signEvent = new SignInteractEvent(sign, this, user);
            redSmokes.getServer().getPluginManager().callEvent(signEvent);
            if (signEvent.isCancelled()) {
                return false;
            }

            return onSignInteract(sign, user, getUsername(user), redSmokes);
        } catch (final Exception ex) {
            showError(redSmokes, user.getSource(), ex, signName);
            return false;
        }
    }

    protected final boolean onSignBreak(final Block block, final Player player, final IRedSmokes redSmokes) throws MaxMoneyException {
        final ISign sign = new BlockSign(block);
        final User user = redSmokes.getUser(player);
        try {
            if (!(user.isAuthorized("redsmokes.signs." + signName.toLowerCase(Locale.ENGLISH) + ".break") || user.isAuthorized("redsmokes.signs.break." + signName.toLowerCase(Locale.ENGLISH)))) {
                return false;
            }

            final SignBreakEvent signEvent = new SignBreakEvent(sign, this, user);
            redSmokes.getServer().getPluginManager().callEvent(signEvent);
            if (signEvent.isCancelled()) {
                return false;
            }

            return onSignBreak(sign, user, getUsername(user), redSmokes);
        } catch (final SignException ex) {
            showError(redSmokes, user.getSource(), ex, signName);
            return false;
        }
    }

    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IRedSmokes ess) throws SignException, ChargeException {
        return true;
    }

    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IRedSmokes ess) throws SignException, ChargeException, MaxMoneyException {
        return true;
    }

    protected boolean onSignBreak(final ISign sign, final User player, final String username, final IRedSmokes ess) throws SignException, MaxMoneyException {
        return true;
    }

    protected final boolean onBlockPlace(final Block block, final Player player, final IRedSmokes redSmokes) {
        final User user = redSmokes.getUser(player);
        try {
            return onBlockPlace(block, user, getUsername(user), redSmokes);
        } catch (final ChargeException | SignException ex) {
            showError(redSmokes, user.getSource(), ex, signName);
        }
        return false;
    }

    protected final boolean onBlockInteract(final Block block, final Player player, final IRedSmokes redSmokes) {
        final User user = redSmokes.getUser(player);
        try {
            return onBlockInteract(block, user, getUsername(user), redSmokes);
        } catch (final ChargeException | SignException ex) {
            showError(redSmokes, user.getSource(), ex, signName);
        }
        return false;
    }

    protected final boolean onBlockBreak(final Block block, final Player player, final IRedSmokes redSmokes) throws MaxMoneyException {
        final User user = redSmokes.getUser(player);
        try {
            return onBlockBreak(block, user, getUsername(user), redSmokes);
        } catch (final SignException ex) {
            showError(redSmokes, user.getSource(), ex, signName);
        }
        return false;
    }

    protected boolean onBlockBreak(final Block block, final IRedSmokes redSmokes) {
        return true;
    }

    protected boolean onBlockExplode(final Block block, final IRedSmokes redSmokes) {
        return true;
    }

    protected boolean onBlockBurn(final Block block, final IRedSmokes redSmokes) {
        return true;
    }

    protected boolean onBlockIgnite(final Block block, final IRedSmokes redSmokes) {
        return true;
    }

    protected boolean onBlockPush(final Block block, final IRedSmokes redSmokes) {
        return true;
    }

    protected boolean onBlockPlace(final Block block, final User player, final String username, final IRedSmokes redSmokes) throws SignException, ChargeException {
        return true;
    }

    protected boolean onBlockInteract(final Block block, final User player, final String username, final IRedSmokes redSmokes) throws SignException, ChargeException {
        return true;
    }

    protected boolean onBlockBreak(final Block block, final User player, final String username, final IRedSmokes redSmokes) throws SignException, MaxMoneyException {
        return true;
    }

    public Set<Material> getBlocks() {
        return EMPTY_SET;
    }

    public boolean areHeavyEventRequired() {
        return false;
    }

    private String getSignText(final ISign sign, final int lineNumber) {
        return sign.getLine(lineNumber).trim();
    }

    protected final void validateTrade(final ISign sign, final int index, final IRedSmokes redSmokes) throws SignException {
        final String line = getSignText(sign, index);
        if (line.isEmpty()) {
            return;
        }
        final Trade trade = getTrade(sign, index, 0, redSmokes);
        final BigDecimal money = trade.getMoney();
        if (money != null) {
            sign.setLine(index, NumberUtil.shortCurrency(money, redSmokes));
        }
    }

    protected final void validateTrade(final ISign sign, final int amountIndex, final int itemIndex, final User player, final IRedSmokes redSmokes) throws SignException {
        final String itemType = getSignText(sign, itemIndex);
        if (itemType.equalsIgnoreCase("exp") || itemType.equalsIgnoreCase("xp")) {
            final int amount = getIntegerPositive(getSignText(sign, amountIndex));
            sign.setLine(amountIndex, Integer.toString(amount));
            sign.setLine(itemIndex, "exp");
            return;
        }
        final Trade trade = getTrade(sign, amountIndex, itemIndex, player, redSmokes);
        final ItemStack item = trade.getItemStack();
        sign.setLine(amountIndex, Integer.toString(item.getAmount()));
        sign.setLine(itemIndex, itemType);
    }

    protected final Trade getTrade(final ISign sign, final int amountIndex, final int itemIndex, final User player, final IRedSmokes redSmokes) throws SignException {
        return getTrade(sign, amountIndex, itemIndex, player, false, redSmokes);
    }

    protected final Trade getTrade(final ISign sign, final int amountIndex, final int itemIndex, final User player, final boolean allowId, final IRedSmokes redSmokes) throws SignException {
        final String itemType = getSignText(sign, itemIndex);
        if (itemType.equalsIgnoreCase("exp") || itemType.equalsIgnoreCase("xp")) {
            final int amount = getIntegerPositive(getSignText(sign, amountIndex));
            return new Trade(amount, redSmokes);
        }
        final ItemStack item = getItemStack(itemType, 1, allowId, redSmokes);
        final int amount = Math.min(getIntegerPositive(getSignText(sign, amountIndex)), item.getType().getMaxStackSize() * player.getBase().getInventory().getSize());
        if (item.getType() == Material.AIR || amount < 1) {
            throw new SignException(tl("moreThanZero"));
        }
        item.setAmount(amount);
        return new Trade(item, redSmokes);
    }

    protected final void validateInteger(final ISign sign, final int index) throws SignException {
        final String line = getSignText(sign, index);
        if (line.isEmpty()) {
            throw new SignException("Empty line " + index);
        }
        final int quantity = getIntegerPositive(line);
        sign.setLine(index, Integer.toString(quantity));
    }

    protected final int getIntegerPositive(final String line) throws SignException {
        final int quantity = getInteger(line);
        if (quantity < 1) {
            throw new SignException(tl("moreThanZero"));
        }
        return quantity;
    }

    protected final int getInteger(final String line) throws SignException {
        try {
            return Integer.parseInt(line);
        } catch (final NumberFormatException ex) {
            throw new SignException("Invalid sign", ex);
        }
    }

    protected final ItemStack getItemStack(final String itemName, final int quantity, final IRedSmokes redSmokes) throws SignException {
        return getItemStack(itemName, quantity, false, redSmokes);
    }

    protected final ItemStack getItemStack(final String itemName, final int quantity, final boolean allowId, final IRedSmokes redSmokes) throws SignException {
        if (allowId && redSmokes.getSettings().allowOldIdSigns()) {
            final Material newMaterial = redSmokes.getItemDb().getFromLegacy(itemName);
            if (newMaterial != null) {
                return new ItemStack(newMaterial, quantity);
            }
        }

        try {
            final ItemStack item = redSmokes.getItemDb().get(itemName);
            item.setAmount(quantity);
            return item;
        } catch (final Exception ex) {
            throw new SignException(ex.getMessage(), ex);
        }
    }

    protected final ItemStack getItemMeta(final ItemStack item, final String meta, final IRedSmokes redSmokes) throws SignException {
        ItemStack stack = item;
        try {
            if (!meta.isEmpty()) {
                final MetaItemStack metaStack = new MetaItemStack(stack);
                final boolean allowUnsafe = redSmokes.getSettings().allowUnsafeEnchantments();
                metaStack.addStringMeta(null, allowUnsafe, meta, redSmokes);
                stack = metaStack.getItemStack();
            }
        } catch (final Exception ex) {
            throw new SignException(ex.getMessage(), ex);
        }
        return stack;
    }

    protected final BigDecimal getMoney(final String line, final IRedSmokes redSmokes) throws SignException {
        final boolean isMoney = line.matches("^[^0-9-.]?[.0-9]+[^0-9-.]?$");
        return isMoney ? getBigDecimalPositive(line, redSmokes) : null;
    }

    protected final BigDecimal getBigDecimalPositive(final String line, final IRedSmokes redSmokes) throws SignException {
        final BigDecimal quantity = getBigDecimal(line, redSmokes);
        if (quantity.compareTo(MINTRANSACTION) < 0) {
            throw new SignException(tl("moreThanZero"));
        }
        return quantity;
    }

    protected final BigDecimal getBigDecimal(final String line, final IRedSmokes redSmokes) throws SignException {
        try {
            return new BigDecimal(NumberUtil.sanitizeCurrencyString(line, redSmokes));
        } catch (final ArithmeticException | NumberFormatException ex) {
            throw new SignException(ex.getMessage(), ex);
        }
    }

    protected final Trade getTrade(final ISign sign, final int index, final IRedSmokes redSmokes) throws SignException {
        return getTrade(sign, index, 1, redSmokes);
    }

    protected final Trade getTrade(final ISign sign, final int index, final int decrement, final IRedSmokes redSmokes) throws SignException {
        return getTrade(sign, index, decrement, false, redSmokes);
    }

    protected final Trade getTrade(final ISign sign, final int index, final int decrement, final boolean allowId, final IRedSmokes redSmokes) throws SignException {
        final String line = getSignText(sign, index);
        if (line.isEmpty()) {
            return new Trade(signName.toLowerCase(Locale.ENGLISH) + "sign", redSmokes);
        }

        final BigDecimal money = getMoney(line, redSmokes);
        if (money == null) {
            final String[] split = line.split("[ :]+", 2);
            if (split.length != 2) {
                throw new SignException(tl("invalidCharge"));
            }
            final int quantity = getIntegerPositive(split[0]);

            final String item = split[1].toLowerCase(Locale.ENGLISH);
            if (item.equalsIgnoreCase("times")) {
                sign.setLine(index, (quantity - decrement) + " times");
                sign.updateSign();
                return new Trade(signName.toLowerCase(Locale.ENGLISH) + "sign", redSmokes);
            } else if (item.equalsIgnoreCase("exp") || item.equalsIgnoreCase("xp")) {
                sign.setLine(index, quantity + " exp");
                return new Trade(quantity, redSmokes);
            } else {
                final ItemStack stack = getItemStack(item, quantity, allowId, redSmokes);
                sign.setLine(index, quantity + " " + item);
                return new Trade(stack, redSmokes);
            }
        } else {
            return new Trade(money, redSmokes);
        }
    }

    private void showError(final IRedSmokes redSmokes, final CommandSource sender, final Throwable exception, final String signName) {
        redSmokes.showError(sender, exception, "\\ sign: " + signName);
    }

    public interface ISign {
        String getLine(final int index);

        void setLine(final int index, final String text);

        Block getBlock();

        void updateSign();
    }

    static class EventSign implements ISign {
        private final transient SignChangeEvent event;
        private final transient Block block;
        private final transient Sign sign;

        EventSign(final SignChangeEvent event) {
            this.event = event;
            this.block = event.getBlock();
            this.sign = (Sign) block.getState();
        }

        @Override
        public final String getLine(final int index) {
            final StringBuilder builder = new StringBuilder();
            for (final char c : event.getLine(index).toCharArray()) {
                if (c < 0xF700 || c > 0xF747) {
                    builder.append(c);
                }
            }
            return builder.toString();
            //return event.getLine(index); // Above code can be removed and replaced with this line when https://github.com/Bukkit/Bukkit/pull/982 is merged.
        }

        @Override
        public final void setLine(final int index, final String text) {
            event.setLine(index, text);
            sign.setLine(index, text);
            updateSign();
        }

        @Override
        public Block getBlock() {
            return block;
        }

        @Override
        public void updateSign() {
            sign.update();
        }
    }

    static class BlockSign implements ISign {
        private final transient Sign sign;
        private final transient Block block;

        BlockSign(final Block block) {
            this.block = block;
            this.sign = (Sign) block.getState();
        }

        @Override
        public final String getLine(final int index) {
            final StringBuilder builder = new StringBuilder();
            for (final char c : sign.getLine(index).toCharArray()) {
                if (c < 0xF700 || c > 0xF747) {
                    builder.append(c);
                }
            }
            return builder.toString();
            //return event.getLine(index); // Above code can be removed and replaced with this line when https://github.com/Bukkit/Bukkit/pull/982 is merged.
        }

        @Override
        public final void setLine(final int index, final String text) {
            sign.setLine(index, text);
            updateSign();
        }

        @Override
        public final Block getBlock() {
            return block;
        }

        @Override
        public final void updateSign() {
            sign.update();
        }
    }
}
