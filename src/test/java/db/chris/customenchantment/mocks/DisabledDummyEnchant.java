package db.chris.customenchantment.mocks;

import db.chris.customenchantment.api.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class DisabledDummyEnchant extends CustomEnchantment {

    public DisabledDummyEnchant() {
        super("disabledDummy", "Disabled Dummy Enchantment", false);
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
}