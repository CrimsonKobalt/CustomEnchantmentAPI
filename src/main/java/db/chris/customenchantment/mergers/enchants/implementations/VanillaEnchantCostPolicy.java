package db.chris.customenchantment.mergers.enchants.implementations;

import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.mergers.enchants.EnchantingCostPolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Optional;
import java.util.Set;

public class VanillaEnchantCostPolicy implements EnchantingCostPolicy {

    @Override
    public int cost(Enchantment enchantment, int level, Material mat) {
        return level * enchantCost(enchantment, mat);
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