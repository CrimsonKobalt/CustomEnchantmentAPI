package db.chris.customenchantment.anvil.configuration.repair;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.repair.damage.FullRepairMerger;
import db.chris.customenchantment.anvil.configuration.repair.damage.VanillaItemDmgMerger;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ItemDmgMerger {

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

    ItemDmgMerger VANILLA = new VanillaItemDmgMerger();
    ItemDmgMerger FULL = new FullRepairMerger();
}
