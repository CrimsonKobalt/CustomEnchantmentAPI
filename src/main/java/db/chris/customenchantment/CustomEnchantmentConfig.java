package db.chris.customenchantment;

import db.chris.customenchantment.anvil.PreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.RepairPolicy;
import db.chris.customenchantment.anvil.RenameCostPolicy;
import db.chris.customenchantment.mergers.enchants.EnchantingCostPolicy;
import db.chris.customenchantment.mergers.enchants.EnchantmentMerger;
import db.chris.customenchantment.mergers.enchants.implementations.VanillaEnchantmentMerger;
import db.chris.customenchantment.utils.LoreFormatter;
import org.bukkit.inventory.ItemStack;

public interface CustomEnchantmentConfig<E> {

    default LoreFormatter loreFormatter() {
        return LoreFormatter.DEFAULT;
    }

    default EnchantingCostPolicy enchantingCostPolicy() {
        return EnchantingCostPolicy.VANILLA;
    }

    default RepairPolicy repairPolicy() {
        return RepairPolicy.VANILLA;
    }

    default RenameCostPolicy renamePolicy() {
        return RenameCostPolicy.VANILLA;
    }

    default PreviousWorkCostPolicy previousWorkPolicy() {
        return PreviousWorkCostPolicy.VANILLA;
    }

    /** ADVANCED CONFIGURATION : WILL BREAK STUFF IF NOT DONE CORRECTLY **/

    default EnchantmentMerger enchantmentMerger(ItemStack first, ItemStack second) {
        return new VanillaEnchantmentMerger(first, second);
    }


    /** startup & instance fetch methods **/
    default boolean enableCustomAnvilRoutine() {
        return true;
    }

    CustomEnchantmentConfig DEFAULT = new CustomEnchantmentConfig() {};

    static CustomEnchantmentConfig get() {
        return CustomEnchantmentAPI.config;
    }
}