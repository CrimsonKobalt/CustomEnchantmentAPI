package db.chris.customenchantment.anvil;

import db.chris.customenchantment.api.CustomEnchantment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
public class EnchantmentMerger {

    private final Map<Enchantment, Integer> target;
    private final Map<Enchantment, Integer> sacrifice;

    private final Material targetMat;
    private final Material sacrificeMat;
    public EnchantmentMerger(ItemStack target, ItemStack sacrifice) {
        this.target = target.getEnchantments();
        this.targetMat = target.getType();

        this.sacrifice = sacrifice.getEnchantments();
        this.sacrificeMat = sacrifice.getType();
    }

    private boolean prepared = false;
    private int totalCost = 0;
    private final Map<Enchantment, Integer> result = new HashMap<>();

    public int getEnchantmentCost() {
        throwIfNotPrepared();
        return totalCost;
    }

    public Map<Enchantment, Integer> getResultingEnchantments() {
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
        // req: pre-filter out clashes take care of it :)

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
                cost.addAndGet(newLevel * enchantCost(k, sacrificeMat));
            } else {
            // if not,
            //      just add it to result without any cost
                result.put(k, v);
            }
        });

        return cost.get();
    }

    private static int enchantCost(Enchantment enchantment, Material sacrifice) {
        // we need to do something dirty to fix vanilla enchants...
        if (enchantment instanceof CustomEnchantment c) {
            return sacrifice == Material.ENCHANTED_BOOK ? c.getEnchantCostOnBook() : c.getEnchantCostOnItem();
        } else {
            VanillaEnchants v = VanillaEnchants.getAny(enchantment).orElseThrow();
            return sacrifice == Material.ENCHANTED_BOOK ? v.getBook() : v.getItem();
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class VanillaEnchants {

        @EqualsAndHashCode.Include
        private Enchantment key;
        private int book;
        private int item;

        private static final int BOOK_DEFAULT = 2;
        private static final int ITEM_DEFAULT = 4;

        public static Optional<VanillaEnchants> getAny(Enchantment ench) {
            return vanillaEnchants.stream().filter(ve -> ve.getKey().equals(ench)).findAny();
        }

        private static final Set<VanillaEnchants> vanillaEnchants = Set.of(
                new VanillaEnchants(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1),
                new VanillaEnchants(Enchantment.PROTECTION_FIRE, 1, 2),
                new VanillaEnchants(Enchantment.PROTECTION_FALL, 1, 2),
                new VanillaEnchants(Enchantment.PROTECTION_EXPLOSIONS, 2, 4),
                new VanillaEnchants(Enchantment.PROTECTION_PROJECTILE, 1, 2),
                new VanillaEnchants(Enchantment.THORNS, 4, 8),
                // aqua affinity
                new VanillaEnchants(Enchantment.OXYGEN, 2, 4),
                new VanillaEnchants(Enchantment.DEPTH_STRIDER, 2, 4),
                new VanillaEnchants(Enchantment.WATER_WORKER, 2, 4),
                new VanillaEnchants(Enchantment.DAMAGE_ALL, 1, 1),
                new VanillaEnchants(Enchantment.DAMAGE_UNDEAD, 1, 2),
                new VanillaEnchants(Enchantment.DAMAGE_ARTHROPODS, 1, 2),
                new VanillaEnchants(Enchantment.KNOCKBACK, 1, 2),
                new VanillaEnchants(Enchantment.FIRE_ASPECT, 2, 4),
                // looting
                new VanillaEnchants(Enchantment.LOOT_BONUS_MOBS, 2, 4),
                // efficiency?
                new VanillaEnchants(Enchantment.DIG_SPEED, 1, 1),
                new VanillaEnchants(Enchantment.SILK_TOUCH, 4, 8),
                new VanillaEnchants(Enchantment.DURABILITY, 1, 2),
                // fortune?
                new VanillaEnchants(Enchantment.LOOT_BONUS_BLOCKS, 2, 4),
                // power
                new VanillaEnchants(Enchantment.ARROW_DAMAGE, 1, 1),
                // punch
                new VanillaEnchants(Enchantment.ARROW_KNOCKBACK, 2, 4),
                new VanillaEnchants(Enchantment.ARROW_FIRE, 2, 4),
                new VanillaEnchants(Enchantment.ARROW_INFINITE, 4, 8),
                new VanillaEnchants(Enchantment.LUCK, 2, 4),
                new VanillaEnchants(Enchantment.LURE, 2, 4),
                new VanillaEnchants(Enchantment.FROST_WALKER, 2, 4),
                new VanillaEnchants(Enchantment.MENDING, 2, 4),
                new VanillaEnchants(Enchantment.BINDING_CURSE, 4, 8),
                new VanillaEnchants(Enchantment.VANISHING_CURSE, 4, 8),
                new VanillaEnchants(Enchantment.IMPALING, 2, 4),
                new VanillaEnchants(Enchantment.RIPTIDE, 2, 4),
                new VanillaEnchants(Enchantment.LOYALTY, 1, 1),
                new VanillaEnchants(Enchantment.CHANNELING, 4, 8),
                new VanillaEnchants(Enchantment.MULTISHOT, 2, 4),
                new VanillaEnchants(Enchantment.PIERCING, 1, 1),
                new VanillaEnchants(Enchantment.QUICK_CHARGE, 1, 2),
                new VanillaEnchants(Enchantment.SOUL_SPEED, 4, 8),
                new VanillaEnchants(Enchantment.SWIFT_SNEAK, 4, 8),
                new VanillaEnchants(Enchantment.SWEEPING_EDGE, 2, 4)
        );
    }
}