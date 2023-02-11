package db.chris.customenchantment.anvil;

import db.chris.customenchantment.CustomEnchantmentAPI;
import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.mergers.enchants.EnchantmentMerger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Anvil-mode is used to determine how the anvil should behave.
 */
public enum AnvilMode {

    ENCHANT {
        @Override
        public void execute(AnvilInventory anvil, ItemStack result) {
            int cost = anvil.getRepairCost();
            // find out all the enchantments the target can have:
            EnchantmentMerger enchantmentMerger = new EnchantmentMerger(anvil.getItem(0), anvil.getItem(1));
            enchantmentMerger.prepare();
            Map<Enchantment, Integer> toPutOnResult = enchantmentMerger.getResult();
            cost += enchantmentMerger.getCost();

            // find which enchantments are already applied by vanilla
            // filter them out of toPutOnResult
            // and subtract those costs from the anvil-cost
            Map<Enchantment, Integer> alreadyApplied = result.getItemMeta().getEnchants();
            for (Enchantment e : alreadyApplied.keySet()) {
                toPutOnResult.remove(e);
                cost -= CustomEnchantmentAPI.getEnchantingCostPolicy().cost(e, alreadyApplied.get(e), Material.ENCHANTED_BOOK);
            }

            // apply all not-yet applied enchantments, at this point we know they are of type CustomEnchantment
            toPutOnResult.forEach((k, v) -> CustomEnchantment.apply(result, (CustomEnchantment) k, v));
            anvil.setRepairCost(cost);
        }
    },
    MERGE {
        @Override
        public void execute(AnvilInventory anvil, ItemStack result) {
            int cost = anvil.getRepairCost();
            Material mat = anvil.getItem(0).getType();
            // find out all the enchantments the target can have:
            EnchantmentMerger enchantmentMerger = new EnchantmentMerger(anvil.getItem(0), anvil.getItem(1));
            enchantmentMerger.prepare();
            Map<Enchantment, Integer> toPutOnResult = enchantmentMerger.getResult();
            cost += enchantmentMerger.getCost();

            // find which enchantments are already applied by vanilla
            // filter them out of toPutOnResult
            // and subtract those costs from the anvil-cost
            Map<Enchantment, Integer> alreadyApplied = result.getItemMeta().getEnchants();
            for (Enchantment e : alreadyApplied.keySet()) {
                toPutOnResult.remove(e);
                cost -= CustomEnchantmentAPI.getEnchantingCostPolicy().cost(e, alreadyApplied.get(e), mat);
            }

            // apply all not-yet applied enchantments, at this point we know they are of type CustomEnchantment
            toPutOnResult.forEach((k, v) -> CustomEnchantment.apply(result, (CustomEnchantment) k, v));
            anvil.setRepairCost(cost);
        }
    },
    RENAME {
        @Override
        public void execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all customenchants onto result
            anvil.getItem(0).getEnchantments().forEach((k, v) -> {
                if (k instanceof CustomEnchantment c) {
                    CustomEnchantment.apply(result, c, v);
                }
            });

            // repaircost should be figured out by vanilla
        }
    },
    REPAIR {
        @Override
        public void execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all customenchants onto result
            anvil.getItem(0).getEnchantments().forEach((k, v) -> {
                if (k instanceof CustomEnchantment c) {
                    CustomEnchantment.apply(result, c, v);
                }
            });

            // repaircost should be figured out by vanilla
        }
    },
    WAITING {
        @Override
        public void execute(AnvilInventory anvil, ItemStack result) {
            /* do nothing */
        }
    };

    public static AnvilMode findMode(ItemStack first, ItemStack second, ItemStack result) {
        // if there is no item in target-slot, do nothing
        if (first == null) {
            return WAITING;
        }

        // if there is no item in sacrifice slot, check if there is a result.
        if (second == null) {
            if (result == null) {
                return WAITING;
            } else {
                return RENAME;
            }
        }

        // now we know that there is a target and a sacrifice

        // if the sacrifice does not belong to EnchantmentTarget.ALL,
        // we know a repair-action using raw materials is being undertaken
        if (EnchantmentTarget.ALL.includes(second.getType())) {
            return REPAIR;
        }

        if (second.getType().equals(Material.ENCHANTED_BOOK)) {
            return ENCHANT;
        }

        return MERGE;
    }

    public abstract void execute(AnvilInventory anvil, ItemStack result);
}
