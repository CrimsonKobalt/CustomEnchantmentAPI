package db.chris.customenchantment.anvil.configuration.enchant;

import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.utils.LoreBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class has as sole responsibility looking at the items it receives and determining the
 * enchantments that the resulting ItemStack should have, as well as the cost incurred by this enchantment
 */
public interface EnchantmentMerger {

    void apply(ItemStack one, ItemStack two);

    int cost();

    Map<Enchantment, Integer> result();

    /**
     * applies an enchantment to an item at a given level without any checks
     * applies lore to item without any checks
     * @param item item to apply enchantment on
     * @param enchantment enchantment to apply
     * @param level level of enchantment
     */
    static void apply(ItemStack item, Enchantment enchantment, Integer level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        String loreText;
        // vanilla enchantments do not need a lore text for some reason
        if (enchantment instanceof CustomEnchantment c) {
            loreText = c.getDisplayName();
            if (c.hasLevels()) {
                loreText += " " + LoreBuilder.toRomanNumeral(level);
            }
            lore.add(LoreBuilder.formatLore(loreText));
        }
        meta.setLore(lore);
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    }
}
