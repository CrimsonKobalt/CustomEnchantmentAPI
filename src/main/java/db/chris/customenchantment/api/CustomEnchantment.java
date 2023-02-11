package db.chris.customenchantment.api;

import db.chris.customenchantment.utils.LoreBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public abstract class CustomEnchantment extends Enchantment {

    /* NAMING AND AUTODISCOVERY */
    private final String keyName;
    private final String displayName;
    private final boolean autoDiscover;

    /* ENCHANTMENT LEVELLING LOGIC */
    private static final int MINLEVEL = 1;
    private static final int MAXLEVEL_DEFAULT = 1;
    private int maxLevel = MAXLEVEL_DEFAULT;

    protected CustomEnchantment(@NotNull String keyName, @NotNull String displayName, boolean autoDiscover) {
        super(NamespacedKey.minecraft(keyName.toLowerCase()));
        this.keyName = keyName.toLowerCase();
        this.displayName = displayName;
        this.autoDiscover = autoDiscover;
    }

    protected CustomEnchantment(@NotNull String keyName, @NotNull String displayName) {
        this(keyName, displayName, true);
    }

    protected CustomEnchantment(@NotNull String name, boolean autoDiscover) {
        this(name.toLowerCase().split("\\s+")[0], name, autoDiscover);
    }

    protected CustomEnchantment(@NotNull String name) {
        this(name, true);
    }

    /**
     * Some enchantments do not care about levels.
     * Functionally, this is the same as returning getMaxLevel() = 1;
     * @return true if enchantment can be levelled
     */
    public final boolean hasLevels() {
        return maxLevel == MINLEVEL;
    }

    public final boolean isEnabled() {
        return autoDiscover;
    }

    /* CUSTOMENCHANTMENT ABSTRACT METHODS */

    /**
     * EnchantmentTarget is used to determine what items an enchantment is allowed enchant
     * <a href="https://minecraft.fandom.com/wiki/Anvil_mechanics#Combining_items">wiki</a>
     * NOTE: EnchantmentTarget.WEAPON == swords
     */
    @NotNull
    @Override
    public abstract EnchantmentTarget getItemTarget();

    /**
     * checks whether 2 enchantments clash
     * @param enchantment The enchantment to check against
     * @return whether the enchantments could both enchant the same item,
     * if said item allows those enchantments to be on it
     */
    @Override
    public abstract boolean conflictsWith(@NotNull Enchantment enchantment);

    /**
     * This method should return the cost of adding this enchantment to an item from an enchanted book
     * (vanilla: 1-4)
     * @return cost
     */
    public abstract int getEnchantCostOnBook();

    /**
     * This method should return the cost of adding this enchantment to an item from another item
     * (vanilla: 1-8)
     * @return cost
     */
    public abstract int getEnchantCostOnItem();

    /**
     * This method should return the maximum level the enchantment can achieve
     * Is your enchantment meant to work without levels? Set this to 1.
     * (Enchantments start at level 1)
     * @return max level of the enchantment
     */
    @Override
    public abstract int getMaxLevel();

    /* INHERITED BY ENCHANTMENT */

    @NotNull
    @Override
    public String getName() {
        return keyName;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }


    /* UTILITIES */

    /**
     * @param enchantments list of enchantments to check against
     * @return whether the target enchantment conflict with any enchantment in the list
     */
    public boolean conflictsWith(Collection<Enchantment> enchantments) {
        return enchantments.stream().anyMatch(this::conflictsWith);
    }

    /**
     * @param enchantments Enchantments to check against
     * @return whether the target enchantment conflict with any of the given enchantments
     */
    public boolean conflictsWith(Enchantment... enchantments) {
        return Arrays.stream(enchantments).anyMatch(this::conflictsWith);
    }

    /**
     * Ability to enchant items is determined by target & other enchantments present
     * ! DOES NOT CHECK IF THE ENCHANTMENT IS ALREADY PRESENT
     * @param itemStack Item to test
     * @return whether an enchantment can enchant an item
     */
    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return this.getItemTarget().includes(itemStack)
                && !this.conflictsWith(itemStack.getEnchantments().keySet());
    }

    public static boolean hasCustomEnchant(ItemStack item) {
        return item.getEnchantments().keySet().stream().anyMatch(e -> e instanceof CustomEnchantment);
    }

    /**
     * applies an enchantment to an item at a given level without any checks
     * applies lore to item without any checks
     * @param item item to apply enchantment on
     * @param enchantment enchantment to apply
     * @param level level of enchantment
     */
    public static void apply(ItemStack item, CustomEnchantment enchantment, Integer level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        String loreText = enchantment.displayName;
        if (enchantment.hasLevels()) {
            loreText += " " + LoreBuilder.toRomanNumeral(level);
        }
        lore.add(LoreBuilder.formatLore(loreText));
        meta.setLore(lore);
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    }
}
