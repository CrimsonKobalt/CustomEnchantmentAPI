package db.chris.customenchantment.api;

import org.bukkit.event.Listener;

public interface DiscoverableListener extends Listener {

    default boolean isEnabled() {
        return true;
    }
}
