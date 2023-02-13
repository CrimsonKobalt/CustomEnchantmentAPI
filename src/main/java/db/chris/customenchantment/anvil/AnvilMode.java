package db.chris.customenchantment.anvil;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.mergers.enchants.EnchantmentMerger;
import db.chris.customenchantment.mergers.enchants.implementations.VanillaEnchantmentMerger;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
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
            return merge(anvil, result, ENCHANT);
        }
    },
    MERGE {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            // fix enchants
            ItemStack toReturn = AnvilMode.merge(anvil, result, MERGE);
            // fix durability of result using sacrifice
            return AnvilMode.repair(anvil, toReturn, MERGE);
        }
    },
    RENAME {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all customenchants onto result
            Map<Enchantment, Integer> enchants = anvil.getItem(0).getEnchantments();
            for (Enchantment e : enchants.keySet()) {
                if (e instanceof CustomEnchantment c) {
                    VanillaEnchantmentMerger.apply(result, c, enchants.get(c));
                }
            }

            int cost = CustomEnchantmentConfig.get().renamePolicy().cost(anvil, result, RENAME);

            // repaircost should be figured out by vanilla
            return result;
        }
    },
    REPAIR {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            // all we need to do is transfer all customenchants onto result
            Map<Enchantment, Integer> enchants = anvil.getItem(0).getEnchantments();
            for (Enchantment e : enchants.keySet()) {
                if (e instanceof CustomEnchantment c) {
                    VanillaEnchantmentMerger.apply(result, c, enchants.get(c));
                }
            }

            // fix resulting durability
            // repaircost should be figured out by vanilla
            return AnvilMode.repair(anvil, result, REPAIR);
        }
    },
    WAITING {
        @Override
        public ItemStack execute(AnvilInventory anvil, ItemStack result) {
            /* do nothing */
            return result;
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

        log.trace("anvil proposing item merge");
        return MERGE;
    }

    public abstract ItemStack execute(AnvilInventory anvil, ItemStack result);

    private static ItemStack merge(AnvilInventory anvil, ItemStack result, AnvilMode mode) {
        int cost = 0;
        ItemStack first = anvil.getItem(0);
        ItemStack second = anvil.getItem(1);

        boolean sameMaterial = first.getType() == second.getType();
        boolean enchantedBookInSecond = second.getType() == Material.ENCHANTED_BOOK;
        if (!sameMaterial && !enchantedBookInSecond) return result;

        // to get an accurate cost, we'll need to reset result...
        result = first.clone();
        Repairable meta = (Repairable) result.getItemMeta();
        // renaming
        if (!anvil.getRenameText().isBlank()) {
            cost += CustomEnchantmentConfig.get().renamePolicy().cost(anvil, result, mode);
            meta.setDisplayName(anvil.getRenameText());
        }
        meta.setLore(new ArrayList<>());
        meta.setRepairCost(meta.getRepairCost() + 1);
        result.setItemMeta(meta);
        // get rid of enchantments
        if (first.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchMeta = ((EnchantmentStorageMeta) first.getItemMeta());
            Map<Enchantment, Integer> firstMap = enchMeta.getStoredEnchants();
            for (Enchantment e : firstMap.keySet()) {
                enchMeta.removeStoredEnchant(e);
            }
            result.setItemMeta(enchMeta);
        } else {
            for (Enchantment e : result.getEnchantments().keySet()) {
                result.removeEnchantment(e);
            }
        }

        // add costs from previous repair-work
        cost += Math.pow(meta.getRepairCost(), 2) + 1;

        // find out all enchantments the result will have:
        EnchantmentMerger enchantmentMerger = CustomEnchantmentConfig.get().enchantmentMerger(first, second);
        enchantmentMerger.apply();
        Map<Enchantment, Integer> toPutOnResult = enchantmentMerger.result();
        cost += enchantmentMerger.cost();

        // apply all not-yet applied enchantments, at this point we know they are of type CustomEnchantment
        for (Enchantment key : toPutOnResult.keySet()) {
            VanillaEnchantmentMerger.apply(result, key, toPutOnResult.get(key));
        }

        // add previous anvil uses
        log.info("first item: {}", first);
        log.info("second item: {}", second);
        Repairable firstMeta = (Repairable) first.getItemMeta();
        Repairable secondMeta = (Repairable) second.getItemMeta();
        PreviousWorkCostPolicy pwcp = CustomEnchantmentConfig.get().previousWorkPolicy();

        cost += firstMeta.hasRepairCost() ? pwcp.cost(firstMeta.getRepairCost()) : 0;
        cost += secondMeta.hasRepairCost() ? pwcp.cost(secondMeta.getRepairCost()) : 0;
        anvil.setRepairCost(cost);
        return result;
    }

    private static ItemStack repair(AnvilInventory inv, ItemStack result, AnvilMode mode) {
        ItemStack target = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);
        Damageable meta = (Damageable) result.getItemMeta();
        if (meta.isUnbreakable()) return result;
        int resultingDurability = CustomEnchantmentConfig.get().repairPolicy().resultingDamage(target, sacrifice, mode);
        meta.setDamage(resultingDurability);
        result.setItemMeta(meta);
        return result;
    }
}
