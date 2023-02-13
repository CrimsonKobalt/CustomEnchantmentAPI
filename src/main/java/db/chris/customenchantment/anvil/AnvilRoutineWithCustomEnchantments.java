package db.chris.customenchantment.anvil;

import db.chris.customenchantment.CustomEnchantmentConfig;
import db.chris.customenchantment.api.DiscoverableListener;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

@Slf4j
public class AnvilRoutineWithCustomEnchantments implements DiscoverableListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void fixAnvil(PrepareAnvilEvent event) {

        // do some preliminary checks
        AnvilInventory slots = event.getInventory();

        ItemStack first = slots.getItem(0);
        ItemStack second = slots.getItem(1);
        ItemStack result = event.getResult();

        AnvilMode mode = AnvilMode.findMode(first, second, result);
        event.setResult(mode.execute(slots, result));
    }

    @Override
    public boolean isEnabled() {
        return CustomEnchantmentConfig.get().enableCustomAnvilRoutine();
    }
}
