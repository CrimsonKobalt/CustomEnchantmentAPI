package db.chris.customenchantment.anvil.implementations.renaming;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.RenameCostPolicy;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class FreeRenamePolicy implements RenameCostPolicy {

    @Override
    public int cost(AnvilInventory inventory, ItemStack result, AnvilMode mode) {
        return 0;
    }
}
