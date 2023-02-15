package db.chris.customenchantment.anvil.configuration.repair.policy;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.repair.ItemRepairCostPolicy;
import org.bukkit.inventory.ItemStack;

public class FreeRepairPolicy implements ItemRepairCostPolicy {
    @Override
    public int cost(ItemStack one, ItemStack two, AnvilMode mode) {
        return 0;
    }
}
