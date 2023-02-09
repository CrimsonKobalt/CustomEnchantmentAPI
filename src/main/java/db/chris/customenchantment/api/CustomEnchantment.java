package db.chris.customenchantment.api;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    private int maxLevel;
    private int costMultiplierOnBook;
    private int costMultiplierOnItem = costMultiplierOnBook * 2;

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

    /***** CUSTOMENCHANTMENT API *****/

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

    /** INHERITED BY ENCHANTMENT **/
    @NotNull
    @Override
    public String getName() {
        return keyName;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }


    /**
     * EnchantmentTarget is used to determine what items an enchantment is allowed enchant
     * https://minecraft.fandom.com/wiki/Anvil_mechanics#Combining_items
     * NOTE: EnchantmentTarget.WEAPON == swords
     */
    @NotNull
    @Override
    public abstract EnchantmentTarget getItemTarget();

    /**
     * checks whether or not 2 enchantments clash
     * @param enchantment The enchantment to check against
     * @return whether the enchantments could both enchant the same item,
     * if said item allows those enchantments to be on it
     */
    @Override
    public abstract boolean conflictsWith(@NotNull Enchantment enchantment);

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
     * @return whether or not an enchantment can enchant an item
     */
    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return this.getItemTarget().includes(itemStack)
                && this.conflictsWith(itemStack.getEnchantments().keySet());
    }
}
