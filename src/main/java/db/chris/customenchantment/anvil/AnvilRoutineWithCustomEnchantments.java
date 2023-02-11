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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void fixAnvil(PrepareAnvilEvent event) {

        // do some preliminary checks
        AnvilInventory slots = event.getInventory();

        ItemStack first = slots.getItem(0);
        ItemStack second = slots.getItem(1);
        ItemStack result = event.getResult();

        AnvilMode mode = AnvilMode.findMode(first, second, result);

        mode.execute(slots, result);
        event.setResult(result);
    }
}
