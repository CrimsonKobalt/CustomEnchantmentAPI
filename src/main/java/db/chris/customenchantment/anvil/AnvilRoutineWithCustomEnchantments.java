package db.chris.customenchantment.anvil;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

@Slf4j
public class AnvilRoutineWithCustomEnchantments implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void fixAnvil(PrepareAnvilEvent event) {
        // do some preliminary checks
        AnvilInventory slots = event.getInventory();

        ItemStack first = slots.getItem(0);
        ItemStack second = slots.getItem(1);

        if (first == null || second == null) {
            log.trace("null-check failed for {}", this);
            return;
        }

        // The only thing that really matters is normalising the item
        //      -> let vanilla handle most of the issues and focus on enchantments
        EnchantmentMerger enchantmentMerger = new EnchantmentMerger(first, second);
        enchantmentMerger.prepare();

    }
}
