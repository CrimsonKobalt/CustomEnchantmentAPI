package db.chris.customenchantment.mergers.enchants;

import org.bukkit.enchantments.Enchantment;
import java.util.Map;

/**
 * This class has as sole responsibility looking at the items it receives and determining the
 * enchantments that the resulting ItemStack should have, as well as the cost incurred by this enchantment
 */
public interface EnchantmentMerger {

    void apply();

    int cost();

    Map<Enchantment, Integer> result();

}
