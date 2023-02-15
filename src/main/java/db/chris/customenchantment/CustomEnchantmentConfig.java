package db.chris.customenchantment;

import db.chris.customenchantment.anvil.configuration.previouswork.PreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.configuration.repair.ItemDmgMerger;
import db.chris.customenchantment.anvil.configuration.rename.RenameCostPolicy;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantingCostPolicy;
import db.chris.customenchantment.anvil.configuration.enchant.EnchantmentMerger;
import db.chris.customenchantment.anvil.configuration.enchant.merger.VanillaEnchantmentMerger;
import db.chris.customenchantment.anvil.configuration.repair.ItemRepairCostPolicy;
import db.chris.customenchantment.utils.LoreFormatter;

public interface CustomEnchantmentConfig {

    default LoreFormatter loreFormatter() {
        return LoreFormatter.DEFAULT;
    }

    default ItemDmgMerger durabilityAfterRepair() {
        return ItemDmgMerger.VANILLA;
    }

    default EnchantingCostPolicy enchantingCostPolicy() {
        return EnchantingCostPolicy.VANILLA;
    }

    default ItemRepairCostPolicy repairCostPolicy() {
        return ItemRepairCostPolicy.VANILLA;
    }

    default RenameCostPolicy renamePolicy() {
        return RenameCostPolicy.VANILLA;
    }

    default PreviousWorkCostPolicy previousWorkCostPolicy() {
        return PreviousWorkCostPolicy.VANILLA;
    }

    /** ADVANCED CONFIGURATION : WILL BREAK STUFF IF NOT DONE CORRECTLY **/

    default EnchantmentMerger enchantmentMerger(EnchantingCostPolicy policy) {
        return new VanillaEnchantmentMerger(policy);
    }

    /** startup & instance fetch methods **/


    CustomEnchantmentConfig DEFAULT = new CustomEnchantmentConfig() {};

    static CustomEnchantmentConfig get() {
        return CustomEnchantmentAPI.config;
    }
}