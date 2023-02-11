package db.chris.customenchantment.mergers.enchants;

import db.chris.customenchantment.mergers.enchants.implementations.NoCostEnchantingPolicy;
import db.chris.customenchantment.mergers.enchants.implementations.VanillaEnchantCostPolicy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@FunctionalInterface
public interface EnchantingCostPolicy {

    int cost(Enchantment enchantment, int level, Material mat);

    EnchantingCostPolicy VANILLA = new VanillaEnchantCostPolicy();
    EnchantingCostPolicy NOCOST = new NoCostEnchantingPolicy();
}