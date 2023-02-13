package db.chris.customenchantment.anvil.implementations.durability;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.RepairPolicy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class VanillaRepairPolicy implements RepairPolicy {

    @Override
    public int resultingDamage(ItemStack target, ItemStack sacrifice, AnvilMode mode) {
        switch (mode) {
            case MERGE -> {
                return merge(target, sacrifice);
            }
            case REPAIR -> {
                return repair(target, sacrifice);
            }
            default -> throw new IllegalArgumentException("Only AnvilMode MERGE and REPAIR are supported here for now...");
        }
    }

    private static int merge(ItemStack target, ItemStack sacrifice) {
        Damageable targetMeta = (Damageable) target.getItemMeta();
        Damageable sacrificeMeta = (Damageable) sacrifice.getItemMeta();

        if (!targetMeta.hasDamage()) return 0;
        int damage = targetMeta.getDamage();
        int maxDurability = target.getType().getMaxDurability();
        int dmgToRepair = (maxDurability - sacrificeMeta.getDamage()) + (int) Math.ceil(0.12 * maxDurability);

        return Math.max(damage - dmgToRepair, 0);
    }
    private static int repair(ItemStack fix, ItemStack sacrifice) {
        Damageable meta = (Damageable) fix.getItemMeta();
        int count = Math.max(sacrifice.getAmount(), 4);
        int maxDurability = fix.getType().getMaxDurability();
        int dmgToRepair = (int) Math.max(maxDurability, 0.25 * count * maxDurability);
        return Math.max(0, meta.getDamage() - dmgToRepair);
    }


}
