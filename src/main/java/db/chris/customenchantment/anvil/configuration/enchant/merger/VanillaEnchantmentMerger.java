package db.chris.customenchantment.anvil.configuration.enchant.merger;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantingCostPolicy;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantmentMerger;
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

    private final EnchantingCostPolicy policy;

    private Map<Enchantment, Integer> target;
    private Map<Enchantment, Integer> sacrifice;

    private Material targetMat;
    private Material sacrificeMat;

    /**
     * Constructor is NOT null-safe.
     * @param policy, used to calculate the cost of merging
     */
    public VanillaEnchantmentMerger(EnchantingCostPolicy policy) {
        this.policy = policy;

    }

    private boolean prepared = false;
    private int totalCost = 0;
    private Map<Enchantment, Integer> result = new HashMap<>();

    private void throwIfNotPrepared() {
        if (!prepared){
            log.error("merger is not yet prepared, please first call the merger.apply()-method");
            throw new RuntimeException("merger not yet prepared to return results");
        }
    }

    @Override
    public final void apply(ItemStack target, ItemStack sacrifice){
        // init
        prepared = false;
        totalCost = 0;
        result = new HashMap<>();
        this.target = target.getType() == Material.ENCHANTED_BOOK ?
                ((EnchantmentStorageMeta) target.getItemMeta()).getStoredEnchants() : target.getEnchantments();
        this.targetMat = target.getType();

        this.sacrifice = sacrifice.getType() == Material.ENCHANTED_BOOK ?
                ((EnchantmentStorageMeta) sacrifice.getItemMeta()).getStoredEnchants() : sacrifice.getEnchantments();
        this.sacrificeMat = sacrifice.getType();

        // do stuff

        // req: pre-filter out clashes && take care of them :)
        // find compatible enchantments
        Map<Enchantment, Integer> compatibles = new HashMap<>();
        totalCost += filterCompatibleInto(compatibles);
        // now we have 2 maps (with "target" being the first one) with all the necessary data
        totalCost += setResultMap(this.target, compatibles);

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
                cost.addAndGet(policy.conflictCost((int) conflicts));
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
}