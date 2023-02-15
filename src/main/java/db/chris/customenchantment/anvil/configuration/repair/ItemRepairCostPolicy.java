package db.chris.customenchantment.anvil.configuration.repair;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.repair.policy.FreeRepairPolicy;
import db.chris.customenchantment.anvil.configuration.repair.policy.VanillaRepairPolicy;
import org.bukkit.inventory.ItemStack;

public interface ItemRepairCostPolicy {

    int cost(ItemStack one, ItemStack two, AnvilMode mode);

    ItemRepairCostPolicy VANILLA = new VanillaRepairPolicy();
    ItemRepairCostPolicy FREE = new FreeRepairPolicy();
}
