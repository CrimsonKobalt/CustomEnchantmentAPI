package db.chris.customenchantment.anvil.configuration.repair.damage;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.repair.ItemDmgMerger;
import org.bukkit.inventory.ItemStack;

public class FullRepairMerger implements ItemDmgMerger {

    @Override
    public int resultingDamage(ItemStack fix, ItemStack sacrifice, AnvilMode mode) {
        return 0;
    }
}
