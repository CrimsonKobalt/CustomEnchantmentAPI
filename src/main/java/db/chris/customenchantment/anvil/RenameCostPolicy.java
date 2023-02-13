package db.chris.customenchantment.anvil;

import db.chris.customenchantment.anvil.implementations.renaming.FreeRenamePolicy;
import db.chris.customenchantment.anvil.implementations.renaming.VanillaRenamePolicy;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface RenameCostPolicy {

    /**
     * calculate the cost of renaming an item in the anvil
     * @param inventory current anvil inventory
     * @param result currently proposed result
     * @param mode operating mode of the anvil (ENCHANT, MERGE, RENAME, REPAIR, WAITING)
     * @return cost of renaming an item
     */
    int cost(AnvilInventory inventory, ItemStack result, AnvilMode mode);

    RenameCostPolicy VANILLA = new VanillaRenamePolicy();
    RenameCostPolicy FREE = new FreeRenamePolicy();
}
