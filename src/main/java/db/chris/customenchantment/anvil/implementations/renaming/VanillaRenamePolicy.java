package db.chris.customenchantment.anvil.implementations.renaming;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.RenameCostPolicy;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class VanillaRenamePolicy implements RenameCostPolicy {

    @Override
    public int cost(AnvilInventory inventory, ItemStack result, AnvilMode mode) {
        if (mode != AnvilMode.RENAME) return inventory.getRenameText().equals("") ? 0 : 1;
        return ((Repairable) inventory.getItem(0).getItemMeta()).getRepairCost() + 1;
    }
}
