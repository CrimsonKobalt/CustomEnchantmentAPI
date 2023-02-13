package db.chris.customenchantment.anvil.implementations.durability;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.RepairPolicy;
import org.bukkit.inventory.ItemStack;

public class RepairFullyPolicy implements RepairPolicy {

    @Override
    public int resultingDamage(ItemStack fix, ItemStack sacrifice, AnvilMode mode) {
        return 0;
    }
}
