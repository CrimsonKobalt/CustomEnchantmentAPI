package db.chris.customenchantment.anvil;

import db.chris.customenchantment.anvil.implementations.durability.RepairFullyPolicy;
import db.chris.customenchantment.anvil.implementations.durability.VanillaRepairPolicy;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface RepairPolicy {

    /**
     * calculate the remaining damage on the resulting item using the target and sacrifice item,
     * assuming the anvil is operating in AnvilMode mode (ENCHANT, MERGE, RENAME, REPAIR, WAITING).
     * As of now, only AnvilMode.MERGE and AnvilMode.REPAIR are supported.
     * @param target (item 1 in anvil)
     * @param sacrifice (item 2 in anvil)
     * @param mode (anvil operating mode)
     * @return damage on the resulting item
     */
    int resultingDamage(ItemStack target, ItemStack sacrifice, AnvilMode mode);

    RepairPolicy VANILLA = new VanillaRepairPolicy();
    RepairPolicy FULL = new RepairFullyPolicy();
}
