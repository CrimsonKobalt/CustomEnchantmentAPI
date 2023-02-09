package db.chris.customenchantment;

import db.chris.customenchantment.api.CustomEnchantment;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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
public class CustomEnchantmentAPI extends JavaPlugin {

    private static JavaPlugin plugin;

    /***** AUTODISCOVER *****/

    public static void start(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
        // discovery-magic
        Reflections reflector = createReflector();
        discover(reflector, Listener.class, listenersToActivate);
        discover(reflector, CustomEnchantment.class, enchantmentsToRegister);

        // configure enchantments
        registerEnchantments();
        activateListeners(plugin);
    }

    private static Reflections createReflector() {
        // enable usage of this as a library by scanning all classpaths for possible children of CustomEnchantment
        Collection<URL> allPackagePrefixes = Arrays.stream(Package.getPackages())
                .map(p -> p.getName())
                .map(s -> s.split("\\.")[0])
                .distinct()
                .peek(s -> log.debug("discovered package root: {}", s))
                .map(s -> ClasspathHelper.forPackage(s))
                .reduce((c1, c2) -> {
                    Collection<URL> res = new HashSet<>();
                    res.addAll(c1);
                    res.addAll(c2);
                    return res;
                }).orElse(new HashSet<>());

        ConfigurationBuilder config = new ConfigurationBuilder()
                .addUrls(allPackagePrefixes)
                .addScanners(Scanners.SubTypes);

        return new Reflections(config);
    }

    private static <T> void discover(Reflections reflector, Class<T> clazz, List<Class<? extends T>> target) {
        target.addAll(reflector.getSubTypesOf(clazz));
    }

    /***** ENCHANTMENT REGISTRY *****/
    private static final List<Class<? extends CustomEnchantment>> enchantmentsToRegister = new ArrayList<>();
    private static final List<CustomEnchantment> enchantments = new ArrayList<>();

    private static void registerEnchantments() {
        enchantmentsToRegister.stream()
                .peek(clazz -> log.debug("discovered Enchantment: {}", clazz.getSimpleName()))
                .map(CustomEnchantmentAPI::createEnchantment)
                .filter(CustomEnchantment::isEnabled)
                .peek(enchantment -> log.info("registering Enchantment: {}", enchantment.getClass().getSimpleName()))
                .peek(enchantments::add)
                .forEach(CustomEnchantmentAPI::putInRegistry);
    }

    private static void putInRegistry(CustomEnchantment enchantment) {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (NoSuchFieldException nsfe) {
            log.error("enchantment registry failed: class-loading error", nsfe);
            throw new RuntimeException(nsfe);
        } catch (IllegalAccessException iae) {
            log.error("enchantment registry failed: java reflection failed to access field member", iae);
            throw new RuntimeException(iae);
        }
    }

    @SuppressWarnings("unchecked cast")
    public static <T extends CustomEnchantment> T find(Class<T> clazz) {
        return enchantments.stream()
                .filter(e -> e.getClass().equals(clazz))
                .map(e -> (T) e)
                .findFirst()
                .orElseThrow();
    }

    private static <T extends CustomEnchantment> T createEnchantment(Class<T> clazz) {
        try {
            Constructor<T> cons = clazz.getDeclaredConstructor();
            cons.setAccessible(true);
            return cons.newInstance();
        } catch (NoSuchMethodException e) {
            log.error("{} requires a no-args constructor", clazz.getSimpleName());
            throw new RuntimeException("could not contruct " + clazz.getSimpleName(),e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.error("{} failed to construct, cause: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /***** LISTENERS ******/
    private static final List<Class<? extends Listener>> listenersToActivate = new ArrayList<>();

    private static void activateListeners(Plugin plugin) {
        PluginManager manager = plugin.getServer().getPluginManager();
        listenersToActivate.stream()
                .peek(l -> log.debug("discovered Listener: {}", l.getSimpleName()))
                .map(CustomEnchantmentAPI::createListener)
                .peek(listener -> log.info("registering Listener: {}", listener.getClass().getSimpleName()))
                .forEach(listener -> manager.registerEvents(listener, plugin));
    }

    private static <T extends Listener> Listener createListener(Class<T> clazz) {
        try {
            Constructor<T> cons = clazz.getDeclaredConstructor();
            cons.setAccessible(true);
            return cons.newInstance();
        } catch (NoSuchMethodException nsme) {
            log.error("{} required a no-args constructor", clazz.getSimpleName());
            throw new RuntimeException("could not construct " + clazz.getSimpleName(), nsme);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.error("{} failed to construct due to cause: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
