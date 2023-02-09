package db.chris.customenchantment;

import be.seeseemelk.mockbukkit.MockBukkit;
import db.chris.customenchantment.mocks.DisabledDummyEnchant;
import db.chris.customenchantment.mocks.DummyEnchant;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAutoDiscover {

    public Server server;
    public static JavaPlugin plugin;

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
        assertThrows(NoSuchElementException.class, () -> CustomEnchantmentAPI.find(DisabledDummyEnchant.class));
    }
}