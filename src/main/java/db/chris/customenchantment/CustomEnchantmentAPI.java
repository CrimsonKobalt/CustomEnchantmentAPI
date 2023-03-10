package db.chris.customenchantment;

import db.chris.customenchantment.api.CustomEnchantment;
import db.chris.customenchantment.api.DiscoverableListener;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

@Slf4j
public class CustomEnchantmentAPI {

    static CustomEnchantmentConfig config = CustomEnchantmentConfig.DEFAULT;
    static boolean loadAnvilConfig = false;

    public static void setConfig(CustomEnchantmentConfig conf, JavaPlugin plugin) {
        log.warn("Configuration has been changed by: {}", plugin.getClass().getName());
        config = conf;
    }

    /***** AUTODISCOVER *****/

    public static void start(JavaPlugin plugin) {
        // discovery-magic
        Reflections reflector = createReflector(plugin);
        // enchantments
        discover(reflector, CustomEnchantment.class, enchantmentsToRegister);
        registerEnchantments();
        // listeners
        discover(reflector, DiscoverableListener.class, listenersToActivate);
        activateListeners(plugin);
    }

    static void start(JavaPlugin plugin, FileConfiguration config) {
        loadAnvilConfig = config.getBoolean("anvil.fix.enabled");
        log.info("Anvil-fix listener enabled: {}", loadAnvilConfig);
        start(plugin);
    }

    private static Reflections createReflector(JavaPlugin plugin) {
        // enable usage of this as a library by scanning all classpaths for possible children of CustomEnchantment
        Collection<URL> rootPackages = new HashSet<>(
                ClasspathHelper.forPackage(findPluginGroupPackage(plugin), ClassLoader.getSystemClassLoader())
        );

        ConfigurationBuilder config = new ConfigurationBuilder()
                .addUrls(rootPackages);

        return new Reflections(config);
    }

    //this way you scan ALL packages loaded in the classpath: ie all active plugins.
    // Might be useful if I develop this as a stand-alone plugin instead of a library
    private static Collection<URL> getAllRoots() {
        return Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .map(s -> s.split("\\.")[0])
                .distinct()
                .peek(s -> log.debug("Discovered package root: {}", s))
                .map(ClasspathHelper::forPackage)
                .reduce((c1, c2) -> {
                    Collection<URL> res = new HashSet<>();
                    res.addAll(c1);
                    res.addAll(c2);
                    return res;
                }).orElse(new HashSet<>());
    }

    /**
     *
     * @param plugin plugin for which to find the root package
     * @return URL to root package of plugin
     */
    private static URL getPluginRoot(JavaPlugin plugin) {
        String userlib = plugin.getClass().getName();
        try {
            URL rootPackage = ClasspathHelper.forPackage(userlib).stream().findFirst().orElseThrow();
            log.info(rootPackage.getFile());
            return rootPackage;
        } catch (NoSuchElementException e) {
            log.error("couldn't find {} in classpath. Make sure your plugin's main class is in your top-level package.", userlib);
            throw new RuntimeException("couldn't find " + userlib + " in classpath", e);
        }
    }

    private static String findPluginGroupPackage(JavaPlugin plugin) {
        String[] dirs = plugin.getClass().getPackage().getName().split("\\.");
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        for (int i = dirs.length - 1; i>= 0; i--) {
            String pkgName = "";
            for (int j = 0; j <= i ; j++) {
                pkgName += dirs[j] + "/";
            }
            pkgName = pkgName.substring(0, pkgName.length() - 1);
            log.trace("searching pkg: {}", pkgName);
            if (!containsUserClasses(pkgName, loader)) {
                // return package that is one up the tree
                String toReturn = pkgName.replace("/", ".") + "." + dirs[i+1];
                log.trace("discovered first common package: {}", toReturn);
                return toReturn;
            }
        }
        // should never happen in normal package structures...
        throw new IllegalArgumentException("couldn't find own package-root...");
    }

    private static boolean containsUserClasses(String pkgPath, ClassLoader loader) {
        URL pkg = loader.getResource(pkgPath);
        assert pkg != null;
        File pkgf = new File(pkg.getPath());
        if (pkgf.exists() && pkgf.isDirectory()) {
            File[] files = pkgf.listFiles();
            assert files != null;
            for (File file : files) {
                log.trace("found file: {}", file.getName());
                if (file.isFile() && file.getName().endsWith(".class")) {
                    log.trace("{} is a file that ends with .class!", file.getName());
                    return true;
                }
            }
        }
        return false;
    }

    private static <T> void discover(Reflections reflector, Class<T> clazz, List<Class<? extends T>> target) {
        reflector.getSubTypesOf(clazz)
                .stream()
                .peek(t -> log.debug("Discovered {}: {}", clazz.getSimpleName(), t.getSimpleName()))
                .forEach(target::add);
    }

    /***** ENCHANTMENT REGISTRY *****/
    private static final List<Class<? extends CustomEnchantment>> enchantmentsToRegister = new ArrayList<>();
    private static final List<CustomEnchantment> enchantments = new ArrayList<>();

    private static void registerEnchantments() {
        enchantmentsToRegister.stream()
                .map(CustomEnchantmentAPI::createEnchantment)
                .filter(CustomEnchantment::isEnabled)
                .peek(enchantments::add)
                .peek(e -> log.info("Registering CustomEnchantment: {}", e.getClass().getSimpleName()))
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
    public static <T extends CustomEnchantment> T find(Class<? extends CustomEnchantment> clazz) {
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
    private static final List<Class<? extends DiscoverableListener>> listenersToActivate = new ArrayList<>();
    private static final List<DiscoverableListener> enabledListeners = new ArrayList<>();

    private static void activateListeners(Plugin plugin) {
        PluginManager manager = plugin.getServer().getPluginManager();
        listenersToActivate.stream()
                .map(CustomEnchantmentAPI::createListener)
                .peek(listener -> log.info("Registering DiscoverableListener: {}", listener.getClass().getSimpleName()))
                .peek(enabledListeners::add)
                .forEach(listener -> manager.registerEvents(listener, plugin));
    }

    private static <T extends DiscoverableListener> DiscoverableListener createListener(Class<T> clazz) {
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

    @SuppressWarnings("unchecked cast")
    private static <T extends DiscoverableListener> T findListener(Class<? extends DiscoverableListener> toFind) {
        return enabledListeners.stream()
                .filter(l -> l.getClass().equals(toFind))
                .map(l -> (T) l)
                .findFirst().orElse(null);
    }

    public static <T extends DiscoverableListener> boolean isEnabled(Class<T> listenerClass) {
        return findListener(listenerClass) != null;
    }

    public static <T extends DiscoverableListener> void disableListener(Class<T> listenerClass) {
        DiscoverableListener listener = findListener(listenerClass);
        if (listener == null) {
            log.debug("{} is already disabled", listenerClass.getSimpleName());
            return;
        }
        enabledListeners.remove(listener);
        HandlerList.unregisterAll(listener);
        log.info("Disabled listener {}", listenerClass.getSimpleName());
    }

    public static <T extends DiscoverableListener> void enableListener(Class<T> listenerClass, JavaPlugin plugin) {
        DiscoverableListener listener = findListener(listenerClass);
        if (listener != null) {
            log.debug("{} is already enabled", listenerClass.getSimpleName());
            return;
        }
        listener = createListener(listenerClass);
        enabledListeners.add(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        log.info("Enabled listener: {}", listenerClass.getSimpleName());
    }
}
