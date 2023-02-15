package db.chris.customenchantment.anvil.configuration.enchant;

import db.chris.customenchantment.anvil.configuration.enchant.policy.FreeEnchantingPolicy;
import db.chris.customenchantment.anvil.configuration.enchant.policy.VanillaEnchantCostPolicy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public interface EnchantingCostPolicy {

    int cost(Enchantment ench, int level, Material mat);

    int conflictCost(int nrOfConflicts);

    EnchantingCostPolicy VANILLA = new VanillaEnchantCostPolicy();
    EnchantingCostPolicy NOCOST = new FreeEnchantingPolicy();
}