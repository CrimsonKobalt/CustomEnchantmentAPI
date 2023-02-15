package db.chris.customenchantment.anvil.configuration.repair.policy;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.previouswork.PreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.configuration.repair.ItemRepairCostPolicy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.Repairable;

public class VanillaRepairPolicy implements ItemRepairCostPolicy {

    private final PreviousWorkCostPolicy pwork = CustomEnchantmentConfig.get().previousWorkCostPolicy();

    @Override
    public int cost(ItemStack one, ItemStack two, AnvilMode mode) {
        switch (mode) {
            case REPAIR -> {
                if (!((Damageable) one.getItemMeta()).hasDamage()) return 0;
                return pwork.cost(((Repairable) one.getItemMeta()).getRepairCost()) + Math.max(two.getAmount(), 4);
            }
            case MERGE -> {
                if (!((Damageable) one.getItemMeta()).hasDamage()) return 0;
                return 2;
            }
            default -> {
                return 0;
            }
        }
    }
}
