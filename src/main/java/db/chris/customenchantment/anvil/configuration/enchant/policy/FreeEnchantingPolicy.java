package db.chris.customenchantment.anvil.configuration.enchant.policy;

import db.chris.customenchantment.anvil.configuration.enchant.EnchantingCostPolicy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class FreeEnchantingPolicy implements EnchantingCostPolicy {


    @Override
    public int cost(Enchantment ench, int level, Material mat) {
        return 0;
    }

    @Override
    public int conflictCost(int nrOfConflicts) {
        return 0;
    }
}
