package db.chris.customenchantment.mocks;

import db.chris.customenchantment.api.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class DummyEnchant extends CustomEnchantment {

    protected DummyEnchant() {
        super("dummyenchant", "Dummy Enchantment", true);
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public int getEnchantCostOnBook() {
        return 1;
    }

    @Override
    public int getEnchantCostOnItem() {
        return 2;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}