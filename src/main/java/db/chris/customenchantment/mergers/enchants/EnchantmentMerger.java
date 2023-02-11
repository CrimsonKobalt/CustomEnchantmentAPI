package db.chris.customenchantment.mergers.enchants;

import db.chris.customenchantment.CustomEnchantmentAPI;
import db.chris.customenchantment.mergers.Merger;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * takes care of enchantment-merging
 */
@Slf4j
public class EnchantmentMerger implements Merger<Map<Enchantment, Integer>> {

    private final Map<Enchantment, Integer> target;
    private final Map<Enchantment, Integer> sacrifice;

    private final Material targetMat;
    private final Material sacrificeMat;

    /**
     * Constructor is NOT null-safe.
     * @param target first item in anvil
     * @param sacrifice second item in anvil
     */
    public EnchantmentMerger(ItemStack target, ItemStack sacrifice) {
        this.target = target.getEnchantments();
        this.targetMat = target.getType();

        this.sacrifice = sacrifice.getEnchantments();
        this.sacrificeMat = sacrifice.getType();
    }

    private boolean prepared = false;
    private int totalCost = 0;
    private final Map<Enchantment, Integer> result = new HashMap<>();

    @Override
    public int getCost() {
        throwIfNotPrepared();
        return totalCost;
    }

    @Override
    public Map<Enchantment, Integer> getResult() {
        throwIfNotPrepared();
        return result;
    }

    private void throwIfNotPrepared() {
        if (!prepared){
            log.error("merger is not yet prepared, please first call the merger.prepare()-method");
            throw new RuntimeException("merger not yet prepared to return results");
        }
    }
    public final void prepare(){
        if (prepared) return;
        // req: pre-filter out clashes && take care of them :)
        // find compatible enchantments
        Map<Enchantment, Integer> compatibles = new HashMap<>();
        totalCost += filterCompatibleInto(compatibles);
        // now we have 2 maps (with "target" being the first one) with all the necessary data
        totalCost += setResultMap(target, compatibles);

        prepared = true;
    }

    private int filterCompatibleInto(Map<Enchantment, Integer> result) {
        AtomicInteger cost = new AtomicInteger();
        sacrifice.forEach((k, v) -> {
            long conflicts = target.keySet().stream()
                    .filter(k::conflictsWith)
                    .count();
            boolean noConflicts = conflicts == 0L;
            boolean canEnchant = k.getItemTarget().includes(targetMat);
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

        target.forEach((k, v) -> {
            // is the same enchantment present in the other list?
            // if so,
            //      remove enchantment from filtered
            //      find correct enchantment level
            //      add correct cost to ret val
            int newLevel = v;
            if (filtered.containsKey(k)) {
                int other = filtered.get(k);
                filtered.remove(k);

                // new level should be
                //      the same if v > other
                //      v+1 if v == other
                //      not higher than the enchantments maximum level
                newLevel = Math.max(k.getMaxLevel(), Math.max(v, other == v ? v+1 : other));
                cost.addAndGet(CustomEnchantmentAPI.getEnchantingCostPolicy().cost(k, newLevel, sacrificeMat));
            } else {
            // if not,
            //      just add it to result without any cost
                result.put(k, v);
            }
        });

        return cost.get();
    }
}