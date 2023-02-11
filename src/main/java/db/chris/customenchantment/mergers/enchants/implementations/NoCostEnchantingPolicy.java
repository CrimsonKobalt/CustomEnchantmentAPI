package db.chris.customenchantment.mergers.enchants.implementations;

import db.chris.customenchantment.mergers.enchants.EnchantingCostPolicy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class NoCostEnchantingPolicy implements EnchantingCostPolicy {
    @Override
    public int cost(Enchantment enchantment, int level, Material mat) {
        return 0;
    }
}
