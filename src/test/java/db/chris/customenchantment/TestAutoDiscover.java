package db.chris.customenchantment;

import be.seeseemelk.mockbukkit.MockBukkit;
import db.chris.customenchantment.anvil.AnvilRoutineWithCustomEnchantments;
import db.chris.customenchantment.mocks.DisabledDummyEnchant;
import db.chris.customenchantment.mocks.DummyEnchant;
import db.chris.customenchantment.mocks.DummyListener;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class TestAutoDiscover {

    public static Server server;
    public static JavaPlugin plugin;

    private static final Class<DummyListener> clazz = DummyListener.class;

    @BeforeEach
    public void setup() {
        if (server == null) {
            server = MockBukkit.mock();
            plugin = MockBukkit.load(Main.class);
        }
    }

    @Test
    public void testAutoDiscoverOnStartup() {
        CustomEnchantmentAPI.start(plugin);
        assertDoesNotThrow(() -> CustomEnchantmentAPI.find(DummyEnchant.class));
        assertThrows(NoSuchElementException.class, () -> CustomEnchantmentAPI.find(DisabledDummyEnchant.class));
    }

    @Test
    public void testDisableEnabledListeners() {
        assertTrue(CustomEnchantmentAPI.isEnabled(clazz));
        CustomEnchantmentAPI.disableListener(clazz);
        assertDoesNotThrow(() -> CustomEnchantmentAPI.disableListener(clazz));
        assertFalse(CustomEnchantmentAPI.isEnabled(clazz));
    }

    @Test
    public void testEnableDisabledListener() {
        assertTrue(CustomEnchantmentAPI.isEnabled(clazz));
        CustomEnchantmentAPI.disableListener(clazz);
        assertFalse(CustomEnchantmentAPI.isEnabled(clazz));
        CustomEnchantmentAPI.enableListener(clazz, plugin);
        assertTrue(CustomEnchantmentAPI.isEnabled(clazz));
    }
}