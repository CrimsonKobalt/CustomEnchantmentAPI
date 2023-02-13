package db.chris.customenchantment.mergers.enchants.implementations;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.mergers.enchants.EnchantingCostPolicy;
import db.chris.customenchantment.mergers.enchants.EnchantmentMerger;
import db.chris.customenchantment.utils.LoreBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * takes care of enchantment-merging
 */
@Slf4j
public class VanillaEnchantmentMerger implements EnchantmentMerger {

    private final Map<Enchantment, Integer> target;
    private final Map<Enchantment, Integer> sacrifice;

    private final Material targetMat;
    private final Material sacrificeMat;

    /**
     * Constructor is NOT null-safe.
     * @param target first item in anvil
     * @param sacrifice second item in anvil
     */
    public VanillaEnchantmentMerger(ItemStack target, ItemStack sacrifice) {
        this.target = target.getType() == Material.ENCHANTED_BOOK ?
                ((EnchantmentStorageMeta) target.getItemMeta()).getStoredEnchants() : target.getEnchantments();
        this.targetMat = target.getType();

        this.sacrifice = sacrifice.getType() == Material.ENCHANTED_BOOK ?
                ((EnchantmentStorageMeta) sacrifice.getItemMeta()).getStoredEnchants() : sacrifice.getEnchantments();
        this.sacrificeMat = sacrifice.getType();
    }

    private boolean prepared = false;
    private int totalCost = 0;
    private final Map<Enchantment, Integer> result = new HashMap<>();

    private void throwIfNotPrepared() {
        if (!prepared){
            log.error("merger is not yet prepared, please first call the merger.apply()-method");
            throw new RuntimeException("merger not yet prepared to return results");
        }
    }

    @Override
    public final void apply(){
        if (prepared) return;
        // req: pre-filter out clashes && take care of them :)
        // find compatible enchantments
        Map<Enchantment, Integer> compatibles = new HashMap<>();
        totalCost += filterCompatibleInto(compatibles);
        // now we have 2 maps (with "target" being the first one) with all the necessary data
        totalCost += setResultMap(target, compatibles);

        prepared = true;
    }

    @Override
    public int cost() {
        throwIfNotPrepared();
        return totalCost;
    }

    @Override
    public Map<Enchantment, Integer> result() {
        throwIfNotPrepared();
        return result;
    }

    private int filterCompatibleInto(Map<Enchantment, Integer> result) {
        AtomicInteger cost = new AtomicInteger();
        sacrifice.forEach((k, v) -> {
            long conflicts = target.keySet().stream()
                    .filter(ench -> ench.conflictsWith(k) || k.conflictsWith(ench))
                    .count();
            boolean noConflicts = conflicts == 0L;
            boolean canEnchant = k.getItemTarget().includes(targetMat) || targetMat.equals(Material.ENCHANTED_BOOK);
            log.debug("{} has {} conflicting enchantments", k, conflicts);
            // if it can't enchant the ItemTarget, just "return"
            //  -> return in foreach-loop skips a single iteration
            if (!canEnchant) return;

            if (noConflicts) {
                result.put(k, v);
            } else {
                // one level for every conflicting enchantment in target
                // this cast should be safe in practice
                cost.addAndGet((int) conflicts);
            }
        });
        return cost.get();
    }

    private int setResultMap(Map<Enchantment, Integer> target,
                             Map<Enchantment, Integer> filtered) {
        AtomicInteger cost = new AtomicInteger();

        EnchantingCostPolicy policy = CustomEnchantmentConfig.get().enchantingCostPolicy();
        filtered.forEach((k, v) -> {
            // is the same enchantment present in the other list?
            // if so,
            //      remove enchantment from that list
            //      find correct enchantment level
            //      add correct cost
            if (target.containsKey(k)) {
                int other = target.get(k);
                // new level should be
                //      max(v, other) OR
                //      v+1 if v == target.get(k)
                //      AND not higher than the enchantments maximum level
                int newLevel = Math.min(k.getMaxLevel(), Math.max(v, other == v ? v + 1 : other));
                cost.addAndGet(policy.cost(k, newLevel, sacrificeMat));
                target.remove(k);
                result.put(k, newLevel);
            } else {
            // if not,
            //      just add it to result without any cost
                cost.addAndGet(policy.cost(k, v, sacrificeMat));
                result.put(k, v);
            }
        });
        result.putAll(target);

        return cost.get();
    }

    /**
     * applies an enchantment to an item at a given level without any checks
     * applies lore to item without any checks
     * @param item item to apply enchantment on
     * @param enchantment enchantment to apply
     * @param level level of enchantment
     */
    public static void apply(ItemStack item, Enchantment enchantment, Integer level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        String loreText;
        // vanilla enchantments do not need a lore text for some reason
        if (enchantment instanceof CustomEnchantment c) {
            loreText = c.getDisplayName();
            if (c.hasLevels()) {
                loreText += " " + LoreBuilder.toRomanNumeral(level);
            }
            lore.add(LoreBuilder.formatLore(loreText));
        }
        meta.setLore(lore);
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    }
}