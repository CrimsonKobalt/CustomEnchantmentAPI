package db.chris.customenchantment.anvil;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantingCostPolicy;
import db.chris.customenchantment.anvil.configuration.previouswork.PreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.configuration.rename.RenameCostPolicy;
import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantmentMerger;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Anvil-mode is used to determine how the anvil should behave.
 */
@Slf4j(topic = "CustomEnchantmentAPI")
public enum AnvilMode {

    ENCHANT {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            ItemStack target = anvil.getItem(0);
            ItemStack sacrifice = anvil.getItem(1);
            int cost = 0;
            ItemStack toReturn = AnvilMode.createEmptyClone(target);

            // cost due to previous work
            cost += AnvilMode.getCostDueToPreviousWork(target, sacrifice);

            // merge enchants
            cost += AnvilMode.enchantResultAndGetCost(target, sacrifice, toReturn);

            // renaming
            cost += AnvilMode.renameAndGetCost(target, toReturn, anvil.getRenameText(), this);

            anvil.setRepairCost(cost);
            return toReturn;
        }
    },
    MERGE {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            ItemStack target = anvil.getItem(0);
            ItemStack sacrifice = anvil.getItem(1);
            int cost = 0;
            ItemStack toReturn = AnvilMode.createEmptyClone(target);

            // previous work-cost
            cost += AnvilMode.getCostDueToPreviousWork(target, sacrifice);

            // merge enchants
            cost += AnvilMode.enchantResultAndGetCost(target, sacrifice, toReturn);

            // rename
            cost += AnvilMode.renameAndGetCost(target, toReturn, anvil.getRenameText(), this);

            // repair
            cost += AnvilMode.fixDurability(anvil, toReturn, this);

            anvil.setRepairCost(cost);
            return result;
        }
    },
    RENAME {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all customenchants onto result
            AnvilMode.applyCustomEnchants(anvil, result);

            int cost = CustomEnchantmentConfig.get().renamePolicy().cost(
                    this,
                    !anvil.getRenameText().equals(""),
                    ((Repairable) anvil.getItem(0)).getRepairCost()
            );

            anvil.setRepairCost(cost);
            return result;
        }
    },
    REPAIR {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all custom-enchants onto result
            AnvilMode.applyCustomEnchants(anvil, result);

            // and fix resulting durability & repair-cost
            int cost = fixDurability(anvil, result, this);
            cost += CustomEnchantmentConfig.get().renamePolicy().cost(
                    this,
                    !anvil.getRenameText().equals(""),
                    ((Repairable) anvil.getItem(0)).getRepairCost()
            );

            anvil.setRepairCost(cost);
            return result;
        }
    },
    WAITING {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            /* do nothing */
            return null;
        }
    };

    public static AnvilMode findMode(ItemStack first, ItemStack second, ItemStack result) {
        // if there is no item in target-slot, do nothing
        if (first == null) {
            log.trace("anvil is waiting");
            return WAITING;
        }

        // if there is no item in sacrifice slot, check if there is a result.
        if (second == null) {
            if (result == null) {
                log.trace("anvil is waiting");
                return WAITING;
            } else {
                log.trace("anvil proposing item rename");
                return RENAME;
            }
        }

        // now we know that there is a target and a sacrifice

        // if the sacrifice does not belong to EnchantmentTarget.ALL,
        // we know a repair-action using raw materials is being undertaken
        if (!EnchantmentTarget.ALL.includes(second.getType())
                && !second.getType().equals(Material.ENCHANTED_BOOK)) {
            log.trace("anvil proposing item repair");
            return REPAIR;
        }

        if (second.getType().equals(Material.ENCHANTED_BOOK)) {
            log.trace("anvil proposing enchantment");
            return ENCHANT;
        }

        if (first.getType() != second.getType()) return WAITING;

        log.trace("anvil proposing item merge");
        return MERGE;
    }

    public abstract ItemStack execute(AnvilInventory anvil, ItemStack result);

    /**
     * fixes durability of item + returns cost of repair
     * @param inv inv
     * @param result result
     * @return ItemStack with the correct durability
     */
    private static int fixDurability(AnvilInventory inv, ItemStack result, AnvilMode mode) {
        ItemStack target = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);
        Damageable meta = (Damageable) result.getItemMeta();
        if (meta.isUnbreakable()) return 0;
        int cost = CustomEnchantmentConfig.get().repairCostPolicy().cost(target, sacrifice, mode);
        int resultingDurability = CustomEnchantmentConfig.get().durabilityAfterRepair().resultingDamage(target, sacrifice, mode);
        meta.setDamage(resultingDurability);
        result.setItemMeta(meta);
        inv.setRepairCost(cost);
        return cost;
    }

    /**
     * applies the enchantments to the result & fixes lore
     * @param anvil anvil-inventory
     * @param result resulting item to which to apply the enchants
     */
    private static void applyCustomEnchants(AnvilInventory anvil, ItemStack result) {
        Map<Enchantment, Integer> enchants = anvil.getItem(0).getEnchantments();
        for (Enchantment e : enchants.keySet()) {
            if (e instanceof CustomEnchantment c) {
                EnchantmentMerger.apply(result, c, enchants.get(c));
            }
        }
    }

    /**
     * clones an ItemStack without enchantments & lore, and increases previous work on cloned item by one
     * @param toClone first itemstack
     * @return cloned object without enchantments and lore, and with previous-work increased by one
     */
    private static ItemStack createEmptyClone(ItemStack toClone) {
        ItemStack result = toClone.clone();
        // +1 the work-cost
        Repairable meta = (Repairable) result.getItemMeta();
        meta.setRepairCost(meta.getRepairCost() + 1);
        // empty lore
        meta.setLore(new ArrayList<>());
        result.setItemMeta(meta);
        // get rid of enchantments already on this item
        if (result.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchMeta = ((EnchantmentStorageMeta) result.getItemMeta());
            Map<Enchantment, Integer> enchants = enchMeta.getStoredEnchants();
            for (Enchantment enchantment : enchants.keySet()) {
                enchMeta.removeStoredEnchant(enchantment);
            }
            result.setItemMeta(enchMeta);
        } else {
            Map<Enchantment, Integer> enchants = result.getEnchantments();
            for (Enchantment enchantment : enchants.keySet()) {
                result.removeEnchantment(enchantment);
            }
        }
        return result;
    }

    private static int getCostDueToPreviousWork(ItemStack target, ItemStack sacrifice) {
        PreviousWorkCostPolicy wpolicy = CustomEnchantmentConfig.get().previousWorkCostPolicy();
        int previousRepairsFirst = ((Repairable) target.getItemMeta()).getRepairCost();
        int previousRepairsSecond = ((Repairable) sacrifice.getItemMeta()).getRepairCost();
        return wpolicy.cost(previousRepairsFirst) + wpolicy.cost(previousRepairsSecond);
    }

    private static int enchantResultAndGetCost(ItemStack target, ItemStack sacrifice, ItemStack result) {
        EnchantingCostPolicy enchantingCostPolicy = CustomEnchantmentConfig.get().enchantingCostPolicy();
        EnchantmentMerger merger = CustomEnchantmentConfig.get().enchantmentMerger(enchantingCostPolicy);
        merger.apply(target, sacrifice);
        Map<Enchantment, Integer> enchantments = merger.result();
        for (Enchantment e : enchantments.keySet()) {
            EnchantmentMerger.apply(result, e, enchantments.get(e));
        }
        return merger.cost();
    }

    private static int renameAndGetCost(ItemStack first, ItemStack toReturn, String renameText, AnvilMode mode) {
        RenameCostPolicy renameCostPolicy = CustomEnchantmentConfig.get().renamePolicy();
        int previousRepairsFirst = ((Repairable) first.getItemMeta()).getRepairCost();
        boolean isRenamed = !renameText.equals("");
        if (isRenamed) {
            ItemMeta meta = toReturn.getItemMeta();
            meta.setDisplayName(renameText);
            toReturn.setItemMeta(meta);
        }
        return renameCostPolicy.cost(mode, isRenamed, previousRepairsFirst);
    }
}
