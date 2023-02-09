package db.chris.customenchantment.api;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

@Slf4j
public abstract class CustomEnchantment extends Enchantment {

    /* NAMING AND AUTODISCOVERY */
    private final String keyName;
    private final String displayName;
    private final boolean autoDiscover;

    /* ENCHANTMENT LEVELLING LOGIC */
    private static final int MINLEVEL = 1;
    private int maxLevel;
    private int costMultiplierOnBook;
    private int costMultiplierOnItem = costMultiplierOnBook * 2;

    protected CustomEnchantment(@NotNull String keyName, @NotNull String displayName, boolean autoDiscover) {
        super(NamespacedKey.minecraft(keyName.toLowerCase()));
        this.keyName = keyName.toLowerCase();
        this.displayName = displayName;
        this.autoDiscover = autoDiscover;
    }

    protected CustomEnchantment(@NotNull String keyName, @NotNull String displayName) {
        this(keyName, displayName, true);
    }

    protected CustomEnchantment(@NotNull String name, boolean autoDiscover) {
        this(name, name, autoDiscover);
    }

    protected CustomEnchantment(@NotNull String name) {
        this(name, name, true);
    }

    /***** CUSTOMENCHANTMENT API *****/

    /**
     * Some enchantments do not care about levels.
     * Functionally, this is the same as returning getMaxLevel() = 1;
     * @return true if enchantment can be levelled
     */
    public boolean isLevelled() {
        return maxLevel == MINLEVEL;
    }

    public final boolean isEnabled() {
        return autoDiscover;
    }

    /** INHERITED BY ENCHANTMENT **/
    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return false;
    }
}
