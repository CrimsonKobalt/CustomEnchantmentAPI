package db.chris.customenchantment;

import be.seeseemelk.mockbukkit.MockBukkit;
import db.chris.customenchantment.mocks.DummyEnchant;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestAutoDiscover {

    public Server server;
    public static Plugin plugin;

    @BeforeEach
    public void setup() {
        server = MockBukkit.mock();
        if (plugin == null) {
            plugin = MockBukkit.load(Main.class);
        }
    }

    @Test
    public void testAutoDiscoverOnStartup() {
        assertDoesNotThrow(() -> CustomEnchantmentAPI.start(plugin));
        assertDoesNotThrow(() -> CustomEnchantmentAPI.find(DummyEnchant.class));
    }
}
